package net.shopxx.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.shopxx.util.MySessionContext;

/**
 *  session监听器
 * @author yangli
 *
 */
public class SessionListener implements HttpSessionListener {

	private MySessionContext myc = MySessionContext.getInstance();  
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		// TODO Auto-generated method stub
		  HttpSession session = se.getSession();  
          myc.addSession(session);  
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		// TODO Auto-generated method stub
		   HttpSession session = se.getSession();  
           myc.delSession(session);  
	}

}
