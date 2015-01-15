package com.sap.als.security;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sap.als.persistence.Patient;
import com.sap.als.utils.EmfGenerator;

@SuppressWarnings("unchecked")
public class CustomAuthenticationProvider implements AuthenticationProvider {
	private DataSource ds;
	private EntityManagerFactory emf;
	
	public CustomAuthenticationProvider() throws ServletException {
		emf = EmfGenerator.initEntityManagerFactory(ds);
	}
	
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		HttpSession session = attr.getRequest().getSession(false);
		if (session != null && session.getAttribute("isAuthenticated") != null) {
			return (Authentication)session.getAttribute("auth");
		}
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();
		if (authenticatedInDB(email, password)) {
			List<GrantedAuthority> grantedAuths = getRolesForUser(email);
			Authentication auth = new UsernamePasswordAuthenticationToken(email,
					password, grantedAuths);
			
			if (session == null) {
				session = attr.getRequest().getSession(true);
			}
			session.setAttribute("email", email);
			session.setAttribute("isAuthenticated", true);
			session.setAttribute("auth", auth);
			
			EntityManager em = emf.createEntityManager();
			Query query = em.createNamedQuery("PatientByEmail");
			query.setParameter("email", authentication.getName());
			List<Patient> resultList = query.getResultList();
			session.setAttribute("patientId", resultList.get(0).getId());
			
			return auth;
		} else {
			throw new BadCredentialsException(
					"Unable to authenticate username and password.");
		}
	}

	private List<GrantedAuthority> getRolesForUser(String name) {
		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
		if ("meirav.daniel@sap.com".equals(name)) {
			grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return grantedAuths;
	}

	private boolean authenticatedInDB(String name, String password) {
		EntityManager em = emf.createEntityManager();

		try {
			Query query = em.createNamedQuery("PatientByEmail");
			query.setParameter("email", name);
			@SuppressWarnings("unchecked")
			List<Patient> resultList = query.getResultList();
			if (resultList.size() != 1) {
				return false;
			}
			Patient p = resultList.get(0);
			return HashCode.isPasswordEquel(password, p.getPassword());
		} finally {
			em.close();
		}
	}

	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
