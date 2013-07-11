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
 *******************************************************************************/
package org.robobninjas.riemann.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;

/**
 * An {@link ObjectMapper} that can deserialize Riemann json received via websocket connection.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
public class RiemannEventObjectMapper extends ObjectMapper {

    public RiemannEventObjectMapper() {
        // ISO-8601
        configure(com.fasterxml.jackson.databind.SerializationFeature.
                WRITE_DATES_AS_TIMESTAMPS, false);
        registerModule(new JodaModule());
    }

    public RiemannEvent readEvent(String json) throws IOException {
        return readValue(json, RiemannEvent.class);
    }
}
