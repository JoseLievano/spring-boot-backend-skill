package com.authServer.shared.tools;

import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class URLValidator {

    // Regex to check that the domain does not include protocol and is in a valid domain format.
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?!.*://)(?=.{1,253}$)(?:(?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,63}$"
    );

    public Optional<String> validate(String domain) {
        // Validate domain format
        if (domain == null || domain.isEmpty() || !DOMAIN_PATTERN.matcher(domain).matches()) {
            return Optional.empty();
        }

        // First, try HTTPS with proper SSL validation
        String httpsUrl = "https://" + domain;
        SSLCheckResult sslResult = checkSSL(httpsUrl);

        switch (sslResult) {
            case VALID_SSL:
                return Optional.of(httpsUrl);
            case INVALID_SSL:
                return Optional.of(httpsUrl + "-");
            case NO_SSL:
            case CONNECTION_ERROR:
                // Try HTTP as fallback
                String httpUrl = "http://" + domain;
                if (isReachableViaHttp(httpUrl)) {
                    return Optional.of(httpUrl);
                }
                // If both HTTPS and HTTP fail, return empty
                return Optional.empty();
            case UNREACHABLE:
            default:
                return Optional.empty();
        }
    }

    private enum SSLCheckResult {
        VALID_SSL,
        INVALID_SSL,
        NO_SSL,
        CONNECTION_ERROR,
        UNREACHABLE
    }

    private SSLCheckResult checkSSL(String urlString) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);

            // Add user agent to avoid being blocked
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();

            // If we get here, SSL is valid and site is reachable
            if (responseCode >= 200 && responseCode < 500) {
                return SSLCheckResult.VALID_SSL;
            }
            return SSLCheckResult.VALID_SSL; // Even 5xx errors mean SSL is valid

        } catch (SSLHandshakeException | SSLPeerUnverifiedException e) {
            // SSL certificate exists but is invalid (expired, self-signed, etc.)
            return SSLCheckResult.INVALID_SSL;
        } catch (ConnectException e) {
            // Connection refused - likely no HTTPS service
            return SSLCheckResult.NO_SSL;
        } catch (SocketTimeoutException e) {
            // Timeout - could be firewall or slow server
            return SSLCheckResult.CONNECTION_ERROR;
        } catch (UnknownHostException e) {
            // Domain doesn't exist
            return SSLCheckResult.UNREACHABLE;
        } catch (IOException e) {
            // Check the error message for more specific handling
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("Connection refused") ||
                    message.contains("connect timed out")) {
                    return SSLCheckResult.NO_SSL;
                }
                if (message.contains("unable to find valid certification path")) {
                    return SSLCheckResult.INVALID_SSL;
                }
            }
            // For redirect to HTTP or other IO errors, consider as no SSL
            return SSLCheckResult.CONNECTION_ERROR;
        } catch (ClassCastException e) {
            // Not an HTTPS connection
            return SSLCheckResult.NO_SSL;
        } catch (Exception e) {
            return SSLCheckResult.CONNECTION_ERROR;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isReachableViaHttp(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);

            // Add user agent to avoid being blocked
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();
            // Accept any response that indicates the server is reachable
            return responseCode >= 200 && responseCode < 500;
        } catch (UnknownHostException e) {
            // Domain doesn't exist
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}