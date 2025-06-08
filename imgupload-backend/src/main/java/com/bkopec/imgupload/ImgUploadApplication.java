package com.bkopec.imgupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ImgUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImgUploadApplication.class, args);
        System.out.println("Hello World!");

    }

}
