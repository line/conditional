/*
 * Copyright 2022 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.conditional.examples.springframework;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.conditional.Condition;
import com.linecorp.conditional.ConditionContext;

@SpringBootApplication
public class Application implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private final HttpClient httpClient;
    private final JdbcClient jdbcClient;

    Application(HttpClient httpClient, JdbcClient jdbcClient) {
        this.httpClient = httpClient;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final var httpCall = Condition.of(ctx -> httpClient.call()).alias("HTTP");
        final var jdbcCall = Condition.of(ctx -> jdbcClient.call()).alias("JDBC");
        final var ctx = ConditionContext.of();
        assert httpCall.and(jdbcCall).parallel().matches(ctx);
    }
}
