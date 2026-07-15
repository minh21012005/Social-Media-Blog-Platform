package com.socialmediablog.platform.services.user.application.port.out;

import com.socialmediablog.platform.services.user.application.result.VerifiedGoogleIdentity;

public interface GoogleIdentityVerifier {

    VerifiedGoogleIdentity verify(String credential);
}