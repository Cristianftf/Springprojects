package cu.xetid.cav.pdfsigner.validation.cert;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class SignatureValidationCache {

  public final Cache<String, SignatureValidationCacheItem> ocspCache;
  public final Cache<String, X509CRL> crlCache;
  public final Cache<String, Collection<? extends Certificate>> caIssuersCertificates;

  private SignatureValidationCache() {
    this.ocspCache =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build();
    this.crlCache =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.HOURS).build();
    this.caIssuersCertificates =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.HOURS).build();
  }

  public static SignatureValidationCache getInstance() {
    return InstanceHolder.instance;
  }

  private static class InstanceHolder {

    private static final SignatureValidationCache instance = new SignatureValidationCache();
  }
}
