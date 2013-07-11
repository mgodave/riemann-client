/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.robobninjas.riemann.spring.server;

import com.google.common.io.Resources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A spring bean that starts and stops the riemann process.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@Configuration
public class RiemannProcessConfiguration {

    @Value("${cosmo.riemann.config-resource}")
    private String riemannConfigResourcePath;

    @Bean(destroyMethod = "close")
    public RiemannProcess riemann() {
        return new RiemannProcess(getConfig());
    }

    private Path getConfig() {
        final URL resource = Resources.getResource(riemannConfigResourcePath);
        return Paths.get(resource.getPath());
    }
}
