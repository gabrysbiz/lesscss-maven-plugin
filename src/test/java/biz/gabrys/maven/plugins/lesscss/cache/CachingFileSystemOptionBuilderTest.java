package biz.gabrys.maven.plugins.lesscss.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import biz.gabrys.lesscss.compiler2.BuilderCreationException;
import biz.gabrys.lesscss.compiler2.FileSystemOption;
import biz.gabrys.maven.plugins.lesscss.config.ExtendedFileSystemOption;
import biz.gabrys.maven.plugins.lesscss.config.ExtendedFileSystemOptionBuilder;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm;

public class CachingFileSystemOptionBuilderTest {

    private CachingFileSystemOptionBuilder builder;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        builder = new CachingFileSystemOptionBuilder();
    }

    @Test
    public void build_withAllParameters_returnsOption() {
        final File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/root/file.less");
        final File directory = mock(File.class);
        when(directory.getAbsolutePath()).thenReturn("/root/cache");
        final Map<String, String> originalParameters = new HashMap<String, String>();
        originalParameters.put("name", "value");
        originalParameters.put(CachingFileSystem.PROXY_PREFIX + "name", "value2");
        final ExtendedFileSystemOption option = new ExtendedFileSystemOptionBuilder()//
                .cacheContent(false)//
                .cacheRedirects(true)//
                .className("org.example.FileSystem")//
                .parameters(originalParameters)//
                .dateProvider("org.example.DataProvider", "getDate", false)//
                .build();

        final FileSystemOption result = builder//
                .baseFile(file)//
                .cache(directory, HashAlgorithm.MD5)//
                .fileSystemOption(option)//
                .build();

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CachingFileSystem.BASE_FILE_PARAM, "/root/file.less");
        parameters.put(CachingFileSystem.CACHE_DIRECTORY_PARAM, "/root/cache");
        parameters.put(CachingFileSystem.HASH_ALGORITHM_PARAM, HashAlgorithm.MD5.name());
        parameters.put(CachingFileSystem.CACHE_CONTENT_PARAM, "false");
        parameters.put(CachingFileSystem.CACHE_REDIRECTS_PARAM, "true");
        parameters.put(CachingFileSystem.FILE_SYSTEM_PARAM, "org.example.FileSystem");
        parameters.put(CachingFileSystem.PROXY_PREFIX + "name", "value");
        parameters.put(CachingFileSystem.PROXY_PREFIX + CachingFileSystem.PROXY_PREFIX + "name", "value2");
        assertThat(result).isEqualTo(new FileSystemOption(CachingFileSystem.class, parameters));
    }

    @Test
    public void build_withNoParameters_throwsException() {
        exception.expect(BuilderCreationException.class);
        exception.expectMessage("You forgot to execute \"baseFile\" method");

        builder.build();
    }

    @Test
    public void build_withOnlyBaseFile_throwsException() {
        final File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/root/file.less");

        exception.expect(BuilderCreationException.class);
        exception.expectMessage("You forgot to execute \"cache\" method");

        builder.baseFile(file).build();
    }

    @Test
    public void build_withOnlyBaseFileAndCache_throwsException() {
        final File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/root/file.less");
        final File directory = mock(File.class);
        when(directory.getAbsolutePath()).thenReturn("/root/cache");

        exception.expect(BuilderCreationException.class);
        exception.expectMessage("You forgot to execute \"fileSystemOption\" method");

        builder.baseFile(file).cache(directory, HashAlgorithm.MD5).build();
    }
}
