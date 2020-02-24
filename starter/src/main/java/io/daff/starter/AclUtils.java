package io.daff.starter;

import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

public class AclUtils {
	
	public static String getDigestUserPwd(String id) throws Exception {
		return DigestAuthenticationProvider.generateDigest(id);
	}
	
	public static void main(String[] args) throws Exception {
		String id = "imooc:imooc";
		String idDigested = getDigestUserPwd(id);
		System.out.println(idDigested);
	}
}
