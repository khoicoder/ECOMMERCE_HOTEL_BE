package com.example.scraper.client;

import com.example.BE.exception.UnauthorizedException;
import lombok.Data;
import org.springframework.stereotype.Component;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

@Component
public class TravelokaApiClient {

    public String fetchDataAsString() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.traveloka.com/api/v2/hotel/calendar/hotel"))
                .POST(BodyPublishers.ofString("{\"fields\":[],\"data\":{\"hotelId\":\"9000000961878\",\"ccGuaranteeOptions\":{\"ccInfoPreferences\":[\"CC_TOKEN\"],\"ccGuaranteeRequirementOptions\":[\"CC_GUARANTEE\"]},\"numAdults\":2,\"numChildren\":0,\"numRooms\":1,\"numInfants\":0,\"userInfoSpec\":null,\"currency\":\"VND\",\"locale\":\"vi-VN\",\"sid\":\"MAIN_FUNNEL\",\"rateTypes\":[\"PAY_NOW\",\"PAY_AT_PROPERTY\"],\"yearMonth\":{\"year\":\"2026\",\"month\":\"06\"}},\"clientInterface\":\"mobile\"}"))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "vi-VN,vi;q=0.9,fr-FR;q=0.8,fr;q=0.7,en-US;q=0.6,en;q=0.5")
                .setHeader("cache-control", "no-cache")
                .setHeader("content-type", "application/json")
                .setHeader("origin", "https://www.traveloka.com")
                .setHeader("pragma", "no-cache")
                .setHeader("priority", "u=1, i")
                .setHeader("referer", "https://www.traveloka.com/vi-vn/hotel/vietnam/centara-mirage-resort-mui-ne-9000000961878")
                .setHeader("sec-ch-ua", "\"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"")
                .setHeader("sec-ch-ua-mobile", "?1")
                .setHeader("sec-ch-ua-platform", "\"Android\"")
                .setHeader("sec-fetch-dest", "empty")
                .setHeader("sec-fetch-mode", "cors")
                .setHeader("sec-fetch-site", "same-origin")
                .setHeader("t-a-v", "262337")
                .setHeader("tv-clientsessionid", "T1-web.01KTXFB5A1RQEG5JGH6X0D0DZD")
                .setHeader("tv-country", "VN")
                .setHeader("tv-currency", "VND")
                .setHeader("tv-language", "vi_VN")
                .setHeader("tv-mcc-id", "01KTXFB8N9ZD72JPNMDF0961MT")
                .setHeader("user-agent", "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36")
                .setHeader("www-app-version", "release_webacd_20260608-ba38fe8f04")
                .setHeader("x-client-interface", "mobile")
                .setHeader("x-did", "MDFLVFhGQjRXS0I0RlNDOFdBRUVKTjNORkU=")
                .setHeader("x-domain", "accomSearch")
                .setHeader("x-route-prefix", "vi-vn")
                .setHeader("cookie", "clientSessionId=T1-web.01KTXFB5A1RQEG5JGH6X0D0DZD; tv_cs=1; tv-repeat-visit=true; countryCode=VN; tv_user={\"authorizationLevel\":100,\"id\":null}; _gcl_au=1.1.929414034.1781253119; _gid=GA1.2.2106106109.1781253119; _fwb=94mMk1xnzDEIshzr2PDtiF.1781253119429; __lt__cid=27192cfb-1228-4f15-ae57-d92b54844439; __lt__sid=46c10688-7952b05f; _yjsu_yjad=1781253119.6d2ee430-6eb2-4a5d-95cf-25e1d195443a; _kmpid=km|www.traveloka.com|1781253119577|83d807a4-47be-431f-878d-30d60ee0ddcf; _kmpid=km|traveloka.com|1781253119577|83d807a4-47be-431f-878d-30d60ee0ddcf; _ly_su=1781253119.6d2ee430-6eb2-4a5d-95cf-25e1d195443a; _tt_enable_cookie=1; _ttp=01KTXFB7MMSPMPJK656BM9WB9T_.tt.1; _fbp=fb.1.1781253119690.48673137772636516; _pin_unauth=dWlkPU1HSTVNekEyTURZdE56ZGtNQzAwWkRFM0xUa3dPV1V0TUdFMU5ERTFNbVJtT1dVeQ; tv_mcc_id=01KTXFB8N9ZD72JPNMDF0961MT; g_state={\"i_l\":0,\"i_ll\":1781253136452,\"i_b\":\"6dAMRdoITjA2O0otslQj3+Wgp2AfbyVUdrc8mG6CVdU\",\"i_e\":{\"enable_itp_optimization\":0},\"i_et\":1781253119248}; _cs_ex=1760605804; _cs_c=1; ttcsid_CFNI0BRC77UEUGLEG00G=1781253758482::t5dqmqaH4jl2HDhz6JOQ.1.1781253768494.1; aws-waf-token=4194fe5b-4e66-49f0-bfd3-1a7cba93a90d:NQoAssM98ysWAAAA:1CGVVs5gT+0tjC3KypsKDFGgOupdqwUZ0Q7dn/UZ3XyiW39IJEQxkMTwKZUj47FzIH16YH2feZf09Eed1jyF44nNNjjvV+oT3/GogAxy5oAerloh3SE+9zA+m5se08Xv2GfVJGn7dpCdIrT/mSzgjf278XDZCTAD22DAQl0jQL5vhsxHniKRC4rKdZzYY1hl7STNgl6dNEZmC0iVBWrQDzBKUrMjVisDZR3efw9qkw4vvOV5aDHPgSYN; amp_f4354c=f1gaQMIytdZBqzgN1pey3m...1jqtfb74a.1jqtgkt89.0.3.3; wcs_bt=s_2cb982ada97c:1781254485; __rtbh.lid=%7B%22eventType%22%3A%22lid%22%2C%22id%22%3A%22YXsfJIGJI2g9gfTXCaAl%22%2C%22expiryDate%22%3A%222027-06-12T08%3A54%3A45.834Z%22%7D; _ga=GA1.2.990325149.1781253119; _rdt_uuid=1781253119523.f74b066c-2155-4e2f-accd-56f72091c644; cto_bundle=zGrjrF9ZdjdyUENKclBsRTZIajAxcCUyRlFvZzclMkZzY2xZcGYyciUyQnBTeEdMcGlBS1FZR2h1RzZ3MTcyR05xSWVZNGNXUDlRZGZvaGdXcTdGRFRVM1dqSlBMdXpzUjhRNUdHNGtmYzcwc25BeFFYeFE5QjZaaSUyQjlKa3ByV1E1ZjNEQXZpeTZYVVNZU2F5MHozWmJJbXNjaTY5ZkhaUSUzRCUzRA; amp_1a5adb=6-dGA5WMzfMhKvd6J5htdt...1jqtfb748.1jqtgkub7.2f.3.2i; _derived_epik=dj0yJnU9NE1LeTFhZm1pemN3Sjd2SmEwWXVWQVBLdjJnbDlqUm8mbj14VExMUzhRenl3enh5OVFqUXJoUTVBJm09MSZ0PUFBQUFBR29yeVZnJnJtPTEmcnQ9QUFBQUFHb3J5Vmcmc3A9Mg; ttcsid_CUM82PBC77U4QKJNCRL0=1781253119649::pQypQdkwKiSNfsf-xNi6.1.1781254487024.1; tv_lt=1781254494315; datadome=S7QhyX4ct8rX1EozIrmqbm3Cjfa6ikI4lS~g8Un~M3h6IHpoehiZ0Jm5gAyqpTKvA6qRlaQpiEnMVvLuQbdfooRtaTYkmU06l~dhFX5kMXHsXEelo4cupZQDijTthccu; _ga_RSRSMMBH0X=GS2.1.s1781253119$o1$g1$t1781254499$j47$l0$h1040751397; tvo=L2FwaS9zZW4vc3M=; tvl=qgdHX7GvehrD9XH5a3S4PUiOJGezXQ9yizVaSxTklwrLYY64AE4apiD1qmHRGaV8gGAQoV6xR5wi1hxtboYegx0JoHbuxL9J5IDMykh7yrn/kmgjvZe3CXlrOt5A94G1h8SGYm0D03zEW7S7g02l9zkAPbkMGQ6AJj+0Bs51j2cFGH4v6XiZjqyN1Upw0acOhcE/TA5K+nDsGoUP7XeFzolvbLkN6Nnfv8Z/Pzj6kzQK5fGhoDRia+sHRzXrD+okNjW+NkVXC2BoUdiADUg/No5yW85bVtBDzvq/xMZJueXM0WMA1zSky7I1/5su3gOqU4uuATjUL/xv+Zf6AMMbg7E3Q5vJP9xMX0P4ctkpCrUiE4Xv9aRQYykpxayhdfUOxbVWc9mUJC6U6r8wkx04c+4CDTx/jJt5ZXoEQTwGFtn7uLN2DHaQIg+iOVN7+cyWo5F5SbXIBK6Txw0Zz1r1y2x9MdKJ4pkDWtlriWKe+AsV//ghVEhyyiQoIuSscMFDH+WzeIeWPbsPfCDu9N+3vpUF6IEMclGMvtdbudeGbc8=; tvs=qgdHX7GvehrD9XH5a3S4PXWKx93/3Xi103f/kPpnhg1IQez7AjqOPow88qqCMiL7CqvJjpn5Z2svD8QZzAmUN07gmFQkK2qgsdbWEYgFfB5uk0fUx7sD+NMcK9CmgVocVXCUQH7jQYSGVHd/Q00sukDA955iBvi1BiD11jxKqfL5u09yAru40JsnILlCW/Ax5D4BAMe/fVRBVhvaurA71M5GRYUM+hAgUIt5ITTNlntR7Mdt/3rAU8gxIaZ+STHbg43qby0ICwtMQsEEYY/mJ063CCJo+Z+R9n/X0FgUdFdWi6NKpEDgL3xVNnQjLp40FhrkmWFU4Pn0+ElScsbvvXrkzqhynUbQmlBzgR52CvysStRtF7qElnLdB0dOiDupelfzY6Y55hGYGJh2XVnPh+yPGR6U53Yc9f5PLel5clhR5ZjDc0B2wucQ1FzLnuVi3WGlnSOcmvWmaBAeRxrpnumaURC0pEZeeQCe7188J/C6CG27OJSUuklXWI0BIrNRCFnqYYpMSCRL8yPQAXFPMQ==; sen_t=Adt8GUJ9ZJHKDeXDbxeUYsws3EDB9g8MWJFHoKQ2/t/5bWpFmnX0fPPEcXcSKGiLEYsIVliGZMg2BXw2NJVlRejEftvh5xfODJRLzxZ+MTuUOQIKRbcpv3SvFjo=; ttcsid=1781253119650::zqPNwfTAROut2sbpsRMy.1.1781254487024.0::1.1362360.1366367::1244985.41.279.860::1435041.153.266; _dd_s=rum=0&expire=1781255456864&logs=1&id=55a9fe4b-f354-46ce-a94c-afe97dce96f0&created=1781253117350")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200) {
                return "BODY : "+response.body();
            }
            else{
                throw new UnauthorizedException("API ERROR : "+ response.statusCode());
            }






    }
}