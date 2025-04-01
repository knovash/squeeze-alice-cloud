package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("START TRY");
        String jwtToken;
//        JWT TOKEN: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaXNwbGF5X25hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQnS4iLCJlbWFpbCI6Im5vdmFzaGtpQHlhbmRleC5ydSIsImV4cCI6MTc3NTA1MDQ0NiwiZ2VuZGVyIjoibWFsZSIsImlhdCI6MTc0MzUxMjQ1MSwiaXNzIjoibG9naW4ueWFuZGV4LnJ1IiwianRpIjoiYjM3OGZmOGQtOWYxOS00NjlhLWJiYzgtYjdlODM4NjIzZmNiIiwibG9naW4iOiJub3Zhc2hraSIsIm5hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQndC-0LLQsNGIIiwicHN1aWQiOiIxLkFBdTdPdy5PZGJGdGl4MGlaVlV1Y1pDUVZVQ0VnLkQ4SVZ2M1BwN1NSbVB2eGRkcjlmQUEiLCJ1aWQiOjQwOTgyMTkzOX0.cXtx9xv0-o0Sf88ODnjvhG35bOkAPRcudAmT5A66tXM
//        JWT TOKEN: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaXNwbGF5X25hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQnS4iLCJlbWFpbCI6Im5vdmFzaGtpQHlhbmRleC5ydSIsImV4cCI6MTc3NTA1MDQ0NiwiZ2VuZGVyIjoibWFsZSIsImlhdCI6MTc0MzUxMjQ1MSwiaXNzIjoibG9naW4ueWFuZGV4LnJ1IiwianRpIjoiYjM3OGZmOGQtOWYxOS00NjlhLWJiYzgtYjdlODM4NjIzZmNiIiwibG9naW4iOiJub3Zhc2hraSIsIm5hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQndC-0LLQsNGIIiwicHN1aWQiOiIxLkFBdTdPdy5PZGJGdGl4MGlaVlV1Y1pDUVZVQ0VnLkQ4SVZ2M1BwN1NSbVB2eGRkcjlmQUEiLCJ1aWQiOjQwOTgyMTkzOX0.cXtx9xv0-o0Sf88ODnjvhG35bOkAPRcudAmT5A66tXM
        jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaXNwbGF5X25hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQnS4iLCJlbWFpbCI6Im5vdmFzaGtpQHlhbmRleC5ydSIsImV4cCI6MTc3NTA1MDQ0NiwiZ2VuZGVyIjoibWFsZSIsImlhdCI6MTc0MzUxMjQ1MSwiaXNzIjoibG9naW4ueWFuZGV4LnJ1IiwianRpIjoiYjM3OGZmOGQtOWYxOS00NjlhLWJiYzgtYjdlODM4NjIzZmNiIiwibG9naW4iOiJub3Zhc2hraSIsIm5hbWUiOiLQmtC-0L3RgdGC0LDQvdGC0LjQvSDQndC-0LLQsNGIIiwicHN1aWQiOiIxLkFBdTdPdy5PZGJGdGl4MGlaVlV1Y1pDUVZVQ0VnLkQ4SVZ2M1BwN1NSbVB2eGRkcjlmQUEiLCJ1aWQiOjQwOTgyMTkzOX0.cXtx9xv0-o0Sf88ODnjvhG35bOkAPRcudAmT5A66tXM";

//        jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MTgyMDQ1NDMsImp0aSI6ImY1" +
//                "YzhlMjhiLTljMzYtMTFlYi1hZDUwLTAwMjU5MDkyODk4YSIsImV0OTgzODAzNywiaXNzIjoibGueWFuZGV4LnJ" +
//                "1IiwidWlkIjoxMTQyMzQ1MTU4LCJsb2dpbiI6InluZHgtZWxlbmJhc2tha292YSIsInBzdWlkIjoiMS5BQWNPX2c" +
//                "uaDh6eFQxNGVRSFRMSURYd2s1d203dy50Uks4cIczJiVEp3IiwibmFtZSI6Ilx1MDQxNVx1MDQzYlx2MDQzNVx1MDQ" +
//                "zZFx1MDQzMCBcdTA0MTFcdTA0MzBcdTA0NDFcdTA0M2FcdTA0MzBcdTA0M2FcdTA0M2VcdTA0MzJcdTA0MzAiLCJlbWF" +
//                "pbCI6InluZHgtZWxlbmJhc2tha292YUB5YW5kZXgucnUiLCJiaXJ0aGRheSI6IiIsImdlbmRlciI6bnVsbCwiZGl" +
//                "zcGxheV9uYW1lIjoieW5keC1lbGVuYmFza2Frb3ZhIiwiYXZhdGFyX2lkIjoiMC7wLTAifQ." +
//                "O8NEvhJ0dI0OOnZSc7Bl-TvxZ1_JDrIpb7zYRW9Nzn";

//        String email;
//        email = YandexJwtParserSimple.parseYandexJwt(jwtToken, "email");
//        log.info("EMAIL: " + email);
//
//        String gender;
//        gender = YandexJwtParserSimple.parseYandexJwt(jwtToken, "genjder");
//        log.info("GENDER: " + gender);

    }
}