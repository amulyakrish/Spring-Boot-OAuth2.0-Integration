package com.inn.cafe.controller;

import com.inn.cafe.entity.User;
import com.inn.cafe.repository.UserRepository;

import com.inn.cafe.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam("code") String authCode)
    {
        try
        {
            //Note : Invalid grant means send new authcode
            String endPoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
            params.add("code",authCode);
            params.add("clientId","****");
            params.add("clientSecret","*****");
            params.add("redirect_uri","https://developers.google.com/oauthplayground");
            params.add("grant_type","authorization_code"); // this means saying, I have an auth code which I want to exchange for token
            //if I want refresh token when my token is expired , then I can send refresh_token as grant type

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //like how we fill a form in an appliaction
            HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(params,headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(endPoint, request, Map.class); //map is response datatype

            //we extract token
            String idToken = (String) tokenResponse.getBody().get("id_token");

            //the token is send to google api , which is validated over there and returns user info to client
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token="+idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            if(userInfoResponse.getStatusCode() == HttpStatus.OK)
            {
                Map<String,Object> userResponseMap = userInfoResponse.getBody();
                String email = (String) userResponseMap.get("email");
                //check if any user exists with this email in db , if yes leave
                //else create new user with this email
                UserDetails userDetails = null;
                try
                {
                    userDetails = userDetailsService.loadUserByUsername(email);
                }catch(Exception e)
                {
                    log.warn("No user found with given email : "+email);
                    log.info("Creating user with email {}",email);
                }

                if(userDetails == null)
                {
                    User user = new User();
                    user.setEmail(email);
                    user.setUserName(email);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user.setRoles(Arrays.asList("USER"));
                    userRepository.save(user);


                    /*
                    Note:
                    To keep everything stateless,
                    we will comment below code and generate jwt token
                     */
                    /*
                    userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return ResponseEntity.status(HttpStatus.OK).build();
                    */



                }
                String jwtToken = jwtUtil.generateToken(email); //now this jwtToken will be saved by fronted
                return  ResponseEntity.ok(Collections.singletonMap("token",jwtToken));



            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        }catch(Exception e)
        {
            /**
             * For this error : "No HttpMessageConverter for java.util.HashMap and content type "application/x-www-form-urlencoded""
             * use multivalue map instead of hashmap for form_rul_encoded type
             *
             * MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
             * multiMap.add("fruit", "apple");
             * multiMap.add("fruit", "banana"); // Adds to the list
             *
             * System.out.println(multiMap.get("fruit")); // Output: ["apple", "banana"]
             *
             * so now values will be sent like ?k1=v1&k2=v2
             * else it will be key=value&key=value with hashmap
             */
            log.error("Exception occured : "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
