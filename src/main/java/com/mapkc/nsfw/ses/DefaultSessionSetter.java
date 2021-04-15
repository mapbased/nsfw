//package com.dianziq.nsfw.ses;
//
//import RenderContext;
//
//public class DefaultSessionSetter implements SessionSetter {
//
//	static final String FK_GID = "fk-gid";
//	@Override
//	public void setSession(RenderContext rc) {
//
//		String gid = rc.getCookieValue(FK_GID);
//		if (gid == null || gid.length() < 10) {
//			rc.addCookie(FK_GID, "");
//		}
//
//	}
//
//}
