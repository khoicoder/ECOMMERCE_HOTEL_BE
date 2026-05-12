package com.example.BE.services;

import com.example.BE.config.RedisConfig;
import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.RegisterRequest;
import com.example.BE.dto.UpdateProfileRequest;
import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import com.example.BE.security.JwtUtil;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;

import java.util.concurrent.TimeUnit;

//Admin1@
@Service
@Data
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;


    public AuthResponse login(String username, String password) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        //Refresh Token Stateful
        redisTemplate.opsForValue().set(
                "Refresh"+username,
                refreshToken, 7, TimeUnit.DAYS

        );
        System.out.println(
                "Access Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(accessToken)
        );

        System.out.println(
                "Refresh Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(refreshToken)
        );



        return new AuthResponse(accessToken, refreshToken, user.getUsername(),user.getRole());
    }
    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null
                || registerRequest.getEmail() == null
    ) {
            throw new RuntimeException("Missing data");
        }

        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
        throw new RuntimeException("Username already exists");
        }
        UserModel user = new UserModel();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);

        userRepository.save(user);
        return "User registered successfully";
    }
    public String logout(String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new RuntimeException("invalid token");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        redisTemplate.delete("refresh"+username);
        SecurityContextHolder.clearContext();
        return "logged out successfully";


    }
    public AuthResponse refreshAccessToken(String refreshToken){
        if(!jwtUtil.validateRefreshToken(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }
        String username  = jwtUtil.extractUsername(refreshToken);

        String savedToken = (String)redisTemplate.opsForValue().get("Refresh:"+username);
        if(savedToken==null || savedToken.equals(refreshToken)){
            throw new RuntimeException("User not found hoặc refresh token expired");
        }

        UserModel user = userRepository.findByUsername(username) .orElseThrow(()
                -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(username);

        return new AuthResponse(newAccessToken,refreshToken,
                user.getUsername(),
                user.getRole());

    }
//fix update profile
//PATH : /api/profile-update
//JwtFilter ch?y:........ /api/profile-update
//Hibernate: select um1_0.id,um1_0.avatar_url,um1_0.email,um1_0.password,um1_0.role,um1_0.username from users um1_0 where um1_0.username=?
//Hibernate: select um1_0.id,um1_0.avatar_url,um1_0.email,um1_0.password,um1_0.role,um1_0.username from users um1_0 where um1_0.username=?
//2026-05-12T22:12:48.669+07:00 ERROR 133212 --- [BE] [nio-8080-exec-6] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.RuntimeException: Username already exists] with root cause
//
//java.lang.RuntimeException: Username already exists
//	at com.example.BE.services.AuthService.updateProfile(AuthService.java:124) ~[main/:na]
//	at com.example.BE.controller.ProfileController.updateProfile(ProfileController.java:23) ~[main/:na]
//	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
//	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
//	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:252) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:184) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at org.springframework.web.servlet.FrameworkServlet.doPut(FrameworkServlet.java:914) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:650) ~[tomcat-embed-core-11.0.20.jar:6.1]
//	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.6.jar:7.0.6]
//	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.20.jar:6.1]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-11.0.20.jar:11.0.20]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:110) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:110) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:108) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy.lambda$doFilterInternal$3(FilterChainProxy.java:235) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:371) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.access.intercept.AuthorizationFilter.doFilter(AuthorizationFilter.java:101) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:126) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:120) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:132) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:86) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:100) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:181) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:63) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at com.example.BE.security.JwtFilter.doFilterInternal(JwtFilter.java:62) ~[main/:na]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.CorsFilter.doFilterInternal(CorsFilter.java:91) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.header.HeaderWriterFilter.doHeadersAfter(HeaderWriterFilter.java:90) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:75) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:82) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:69) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.doFilterInternal(WebAsyncManagerIntegrationFilter.java:62) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.session.DisableEncodeUrlFilter.doFilterInternal(DisableEncodeUrlFilter.java:42) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:237) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:195) ~[spring-security-web-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.ServletRequestPathFilter.doFilter(ServletRequestPathFilter.java:52) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:113) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.CompositeFilter.doFilter(CompositeFilter.java:74) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration$CompositeFilterChainProxy.doFilter(WebSecurityConfiguration.java:317) ~[spring-security-config-7.0.4.jar:7.0.4]
//	at org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:355) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:272) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.6.jar:7.0.6]
//	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:492) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1779) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57) ~[tomcat-embed-core-11.0.20.jar:11.0.20]
//	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

    public AuthResponse updateProfile(UpdateProfileRequest request,Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }
        String curName = auth.getName();
        UserModel user =userRepository.findByUsername(curName).orElseThrow(()
                -> new RuntimeException("User not found")
        );

            if(request.getUsername().equals(user.getUsername()) && userRepository.findByUsername(curName).isPresent()) {
                throw new RuntimeException("Username already exists");

            }
            user.setUsername(request.getUsername());
            System.out.println(user.getUsername());
            user.setEmail(request.getEmail());
            System.out.println(user.getEmail());
            user.setAvatarUrl(user.getAvatarUrl());
            System.out.println(user.getAvatarUrl());

        if(request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if(request.getNewPassword() ==null && !passwordEncoder.matches(request.getNewPassword(), user.getPassword()))  {
                throw new RuntimeException("current password does not match");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        }

            userRepository.save(user);
            String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            redisTemplate.opsForValue().set("Refresh"+user.getUsername(), newRefreshToken, 7, TimeUnit.DAYS);

        return new AuthResponse(
                newAccessToken
                ,newRefreshToken
                ,user.getUsername()
                ,user.getRole());
    }
    //identity consistency
    //security context lifecycle
    //token stale data problem ,Stale Token Problem "user experience + security + scalability"



}
