package biz.gabrys.maven.plugins.lesscss.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class HashAlgorithmTest {

    @Test
    @Parameters({ //
            "MD5, 8d777f385d3dfec8815d20f7496026dc", //
            "SHA1, a17c9aaa61e80a1bf71d0d850af4e5baa9800bbd", //
            "SHA256, 3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7", //
            "SHA384, 2039e0f0b92728499fb88e23ebc3cfd0554b28400b0ed7b753055c88b5865c3c2aa72c6a1a9ae0a755d87900a4a6ff41", //
            "SHA512, 77c7ce9a5d86bb386d443bb96390faa120633158699c8844c30b13ab0bf92760b7e4416aea397db91b4ac0e5dd56b8ef7e4b066162ab1fdc088319ce6defc876" //
    })
    public void hash(final HashAlgorithm algorithm, final String expected) {
        final String result = algorithm.getHasher().hash("data");
        assertThat(result).isEqualTo(expected);
    }
}
