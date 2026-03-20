-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn javax.xml.**

# R8 missing classes (optional deps referenced by JDBC/logging libs)
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn com.oracle.bmc.ConfigFileReader
-dontwarn com.oracle.bmc.ConfigFileReader$ConfigFile
-dontwarn com.sun.jna.platform.win32.Sspi$SecBufferDesc
-dontwarn io.opentelemetry.api.**
-dontwarn io.opentelemetry.context.**
-dontwarn java.lang.management.ThreadInfo
-dontwarn java.lang.management.ThreadMXBean
-dontwarn java.sql.JDBCType
-dontwarn javax.management.**
-dontwarn javax.naming.**
-dontwarn javax.security.auth.**
-dontwarn javax.security.sasl.**
-dontwarn net.sf.saxon.**
-dontwarn org.ietf.jgss.**
-dontwarn org.osgi.framework.**

# mysql-connector-j optional integrations (R8 may fail if referenced classes are absent on Android)
-dontwarn com.mchange.v2.c3p0.C3P0ProxyConnection
-dontwarn com.mchange.v2.c3p0.QueryConnectionTester
-dontwarn javax.sql.XAConnection
-dontwarn javax.sql.XADataSource
-dontwarn javax.transaction.xa.XAException
-dontwarn javax.transaction.xa.XAResource
-dontwarn javax.transaction.xa.Xid

# kotlinx.serialization: keep generated serializers and metadata used at runtime
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class ** {
    ** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# If you use polymorphic serialization with @SerialName, you may need keep rules for those models.

# =========================
# JDBC Driver keep rules
# =========================
# On Android release builds (R8 minify), JDBC drivers may be removed because they are loaded
# reflectively by DriverManager/Class.forName and/or via ServiceLoader.
# Keep driver implementations and the java.sql.Driver service provider resource.

# Keep MySQL and PostgreSQL driver classes explicitly (used by Class.forName).
# Note: don't keep entire driver packages to avoid pulling optional integrations
# (c3p0/JNA/OSGi/transaction/awt) into the minification graph on Android.
-keepnames class com.mysql.cj.jdbc.Driver
-keepnames class org.postgresql.Driver
-keep class com.mysql.cj.jdbc.Driver { *; }
-keep class org.postgresql.Driver { *; }

# MySQL Connector/J 在连接过程中会依赖其内部实现进行类型转换/反射，
# release (R8 minify/optimize) 可能导致内部类被重命名/裁剪后出现 ClassCastException。
# 因此这里需要至少保持 mysql-connector-j 的核心包不被混淆/裁剪。
-keep class com.mysql.cj.** { *; }

