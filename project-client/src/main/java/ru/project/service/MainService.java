package ru.project.service;

import ru.project.entity.User;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class MainService implements HttpResponse {
    @Override
    public int statusCode() {
        return 200;
    }

    @Override
    public HttpRequest request() {
        return HttpRequest.newBuilder()
                .GET()
                .build();
    }

    @Override
    public Optional<HttpResponse> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public Object body() {
        User user = new User();
        return user;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public HttpClient.Version version() {
        return null;
    }
}
