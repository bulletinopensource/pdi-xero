/**
 *    Copyright 2015 Bulletin.Net (NZ) Limited : www.bulletin.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bulletin.pdi.xero.step.support;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Andrew Lindesay
 */

public class Helpers {

    /**
     * <p>This can be used with keys and so on in order to obfuscate the string for the logs.</p>
     */

    public static String obfuscateAuthenticationDetailForLog(String value) {
        if (null == value || value.length() < 8) {
            return "?";
        }

        // some certificates are presented with lines such as;
        // -----BEGIN PRIVATE KEY----- at the start and
        // -----END PRIVATE KEY----- at the end; this will remove those.

        value = value.replaceFirst("^[\\s-]+[A-Z ]+[\\s-]+","").replaceAll("[\\s-]+[A-Z ]+[\\s-]+$","");
        return value.substring(0, 2) + "..." + value.substring(value.length() - 2, value.length());
    }

    /**
     * <p>If the value is provided then this method will augment the supplied URL by adding on the
     * query to the URL.</p>
     */

    public static StringBuilder appendUrlQuery(StringBuilder url, String key, String value) {
        if(null==url || 0==url.length()) {
            throw new IllegalArgumentException("the supplied url may not be empty.");
        }

        if(StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("the supplied query key may not be empty.");
        }

        if (StringUtils.isNotBlank(value)) {

            url.append(-1 == url.indexOf("?") ? '?' : '&');
            url.append(key);
            url.append("=");

            try {
                url.append(URLEncoder.encode(value, CharEncoding.UTF_8));
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("the encoding must be supported; " + CharEncoding.UTF_8);
            }
        }

        return url;
    }


}
