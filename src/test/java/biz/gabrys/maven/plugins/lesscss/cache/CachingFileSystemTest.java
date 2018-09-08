package biz.gabrys.maven.plugins.lesscss.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import biz.gabrys.lesscss.compiler2.filesystem.FileData;
import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm.Hasher;

public class CachingFileSystemTest {

    private CachingFileSystem fileSystem;
    private FileSystem proxiedFileSystem;
    private CacheStorage cacheStorage;

    @Before
    public void setup() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            ClassNotFoundException {
        cacheStorage = mock(CacheStorage.class);
        proxiedFileSystem = mock(FileSystem.class);

        fileSystem = spy(CachingFileSystem.class);
        fileSystem.cacheStorage = cacheStorage;
        fileSystem.proxiedFileSystem = proxiedFileSystem;
    }

    @Test
    public void configure() throws Exception {
        fileSystem.cacheStorage = null;
        fileSystem.proxiedFileSystem = null;
        doReturn(cacheStorage).when(fileSystem).createCacheStorage(anyString(), any(Hasher.class));
        doReturn(proxiedFileSystem).when(fileSystem).createFileSystem(anyString());

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("base-file", "/root/file.less");
        parameters.put("cache-directory", "/root/directory");
        parameters.put("hash-algorithm", HashAlgorithm.MD5.name());
        parameters.put("file-system", "org.example.FileSystem");
        parameters.put("cache-redirects", "true");
        parameters.put("cache-content", "true");
        parameters.put("proxy.param1", "value1");
        parameters.put("proxy.proxy.param2", "value2");

        fileSystem.configure(parameters);

        assertThat(fileSystem.baseFile).isSameAs("/root/file.less");
        assertThat(fileSystem.proxiedFileSystem).isSameAs(proxiedFileSystem);
        assertThat(fileSystem.cacheStorage).isSameAs(cacheStorage);
        assertThat(fileSystem.cacheRedirects).isTrue();
        assertThat(fileSystem.cacheContent).isTrue();

        verify(fileSystem).createCacheStorage("/root/directory", HashAlgorithm.MD5.getHasher());
        verify(fileSystem).createFileSystem("org.example.FileSystem");
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(proxiedFileSystem).configure(captor.capture());
        assertThat(captor.getValue()).containsOnly(entry("param1", "value1"), entry("proxy.param2", "value2"));
    }

    @Test
    public void isSupported() {
        when(proxiedFileSystem.isSupported("/dir/file.less")).thenReturn(Boolean.TRUE);
        when(proxiedFileSystem.isSupported("/dir/file.mp3")).thenReturn(Boolean.FALSE);

        assertThat(fileSystem.isSupported("/dir/file.less")).isTrue();
        assertThat(fileSystem.isSupported("/dir/file.mp3")).isFalse();
        verifyZeroInteractions(cacheStorage);
    }

    @Test
    public void normalize() throws Exception {
        final String path = "/dir/../abc.less";
        final String normalized = "/abc.less";
        when(proxiedFileSystem.normalize(path)).thenReturn(normalized);

        final String result = fileSystem.normalize(path);

        assertThat(result).isSameAs(normalized);
        verifyZeroInteractions(cacheStorage);
    }

    @Test
    public void expandRedirection_cacheRedirectsDisabled() throws Exception {
        final String path = "/dir/file.less";
        final String expanded = "/dir/expanded.less";
        when(proxiedFileSystem.expandRedirection(path)).thenReturn(expanded);

        final String result = fileSystem.expandRedirection(path);

        assertThat(result).isSameAs(expanded);
        verifyZeroInteractions(cacheStorage);
    }

    @Test
    public void expandRedirection_cacheRedirectsEnabled_dataHasNotBeenCachedBefore() throws Exception {
        final String path = "/dir/file.less";
        final String expanded = "/dir/expanded.less";
        when(proxiedFileSystem.expandRedirection(path)).thenReturn(expanded);
        fileSystem.cacheRedirects = true;

        final String result = fileSystem.expandRedirection(path);

        assertThat(result).isSameAs(expanded);
        verify(cacheStorage).hasRedirection(path);
        verify(cacheStorage).saveRedirection(path, expanded);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void expandRedirection_cacheRedirectsEnabled_dataHasBeenCachedBefore() throws Exception {
        final String path = "/dir/file.less";
        final String expanded = "/dir/expanded.less";
        fileSystem.cacheRedirects = true;
        when(cacheStorage.hasRedirection(path)).thenReturn(Boolean.TRUE);
        when(cacheStorage.getRedirection(path)).thenReturn(expanded);

        final String result = fileSystem.expandRedirection(path);

        assertThat(result).isSameAs(expanded);
        verify(cacheStorage).hasRedirection(path);
        verify(cacheStorage).getRedirection(path);
        verifyNoMoreInteractions(cacheStorage);
        verifyZeroInteractions(proxiedFileSystem);
    }

    @Test
    public void exists_cacheContentDisabled() throws Exception {
        final String path = "/dir/file.less";
        when(proxiedFileSystem.exists(path)).thenReturn(Boolean.TRUE);

        final boolean result = fileSystem.exists(path);

        assertThat(result).isTrue();
        verifyZeroInteractions(cacheStorage);
    }

    @Test
    public void exists_cacheContentEnabled_dataHasNotBeenCachedBefore_fileExists() throws Exception {
        final String path = "/dir/file.less";
        fileSystem.cacheContent = true;
        when(cacheStorage.hasExistent(path)).thenReturn(Boolean.FALSE);
        when(proxiedFileSystem.exists(path)).thenReturn(Boolean.TRUE);

        final boolean result = fileSystem.exists(path);

        assertThat(result).isTrue();
        verify(cacheStorage).hasExistent(path);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void exists_cacheContentEnabled_dataHasNotBeenCachedBefore_fileDoesNotExist() throws Exception {
        final String path = "/dir/file.less";
        fileSystem.cacheContent = true;
        when(cacheStorage.hasExistent(path)).thenReturn(Boolean.FALSE);
        when(proxiedFileSystem.exists(path)).thenReturn(Boolean.FALSE);

        final boolean result = fileSystem.exists(path);

        assertThat(result).isFalse();
        verify(cacheStorage).hasExistent(path);
        verify(cacheStorage).markAsNonExistent(path);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void exists_cacheContentEnabled_dataHasBeenCachedBefore() throws Exception {
        final String path = "/dir/file.less";
        fileSystem.cacheContent = true;
        when(cacheStorage.hasExistent(path)).thenReturn(Boolean.TRUE);
        when(cacheStorage.isExistent(path)).thenReturn(Boolean.TRUE);

        final boolean result = fileSystem.exists(path);

        assertThat(result).isTrue();
        verify(cacheStorage).hasExistent(path);
        verify(cacheStorage).isExistent(path);
        verifyNoMoreInteractions(cacheStorage);
        verifyZeroInteractions(proxiedFileSystem);
    }

    @Test
    public void fetch_cacheContentDisabled() throws Exception {
        final String path = "/dir/file.less";
        doNothing().when(fileSystem).updateDependencies(path);
        final FileData fileData = mock(FileData.class);
        when(proxiedFileSystem.fetch(path)).thenReturn(fileData);

        final FileData result = fileSystem.fetch(path);

        assertThat(result).isSameAs(fileData);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void fetch_cacheContentEnabled_contentHasNotBeenCachedBefore() throws Exception {
        final String path = "/dir/file.less";
        doNothing().when(fileSystem).updateDependencies(path);
        fileSystem.cacheContent = true;
        final byte[] content = new byte[0];
        final String encoding = "encoding";
        final FileData fileData = new FileData(content, encoding);
        when(proxiedFileSystem.fetch(path)).thenReturn(fileData);

        final FileData result = fileSystem.fetch(path);

        assertThat(result).isSameAs(fileData);

        verify(cacheStorage).readContent(path);
        verify(cacheStorage).saveContent(path, content);
        verify(cacheStorage).saveEncoding(path, encoding);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void fetch_cacheContentEnabled_contentHasBeenCachedBefore() throws Exception {
        final String path = "/dir/file.less";
        doNothing().when(fileSystem).updateDependencies(path);
        fileSystem.cacheContent = true;
        final byte[] content = new byte[0];
        final String encoding = "encoding";
        when(cacheStorage.readContent(path)).thenReturn(content);
        when(cacheStorage.readEncoding(path)).thenReturn(encoding);

        final FileData result = fileSystem.fetch(path);

        assertThat(result).isEqualTo(new FileData(content, encoding));
        verify(cacheStorage).readContent(path);
        verify(cacheStorage).readEncoding(path);
        verifyNoMoreInteractions(cacheStorage);
        verifyZeroInteractions(proxiedFileSystem);
    }

    @Test
    public void updateDependencies_dependenciesDoNotContainPath() throws IOException {
        final String path = "/dir/file.less";
        final String baseFile = "/dir/base.less";
        fileSystem.baseFile = baseFile;
        final Collection<String> dependencies = new ArrayList<String>(Arrays.asList("/dir/dept.less"));
        when(cacheStorage.readDependencies(baseFile)).thenReturn(dependencies);

        fileSystem.updateDependencies(path);

        verify(cacheStorage).readDependencies(baseFile);
        verify(cacheStorage).saveDependencies(baseFile, dependencies);
        verifyNoMoreInteractions(cacheStorage);
    }

    @Test
    public void updateDependencies_dependenciesContainPath() throws IOException {
        final String path = "/dir/file.less";

        final String baseFile = "/dir/base.less";
        fileSystem.baseFile = baseFile;
        final Collection<String> dependencies = Arrays.asList("/dir/dept.less", path);
        when(cacheStorage.readDependencies(baseFile)).thenReturn(dependencies);

        fileSystem.updateDependencies(path);

        verify(cacheStorage).readDependencies(baseFile);
        verifyNoMoreInteractions(cacheStorage);
    }
}
