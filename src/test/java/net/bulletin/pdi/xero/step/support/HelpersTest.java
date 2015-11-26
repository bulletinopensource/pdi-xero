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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Lindesay
 */

public class HelpersTest {

    @Test
    public void testObfuscateAuthenticationDetailForLog_withoutMarkers() {
        String input = "456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv"
                + "abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123";

        // ------------------------------------------------------
        String result = Helpers.obfuscateAuthenticationDetailForLog(input);
        // ------------------------------------------------------

        Assert.assertEquals("45...23", result);
    }

    @Test
    public void testObfuscateAuthenticationDetailForLog_withMarkers() {
        String input = "-----BEGIN PRIVATE KEY-----\n"
                + "456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuv"
                + "abcdefhjklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123"
                + "-----END PRIVATE KEY-----";

        // ------------------------------------------------------
        String result = Helpers.obfuscateAuthenticationDetailForLog(input);
        // ------------------------------------------------------

        Assert.assertEquals("45...23", result);
    }

}
