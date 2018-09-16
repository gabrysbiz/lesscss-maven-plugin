package biz.gabrys.maven.plugins.lesscss.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm.Hasher;
import biz.gabrys.maven.plugins.lesscss.io.FileManager;

public class CachingFileSystemStorageTest {

    private static final String CACHE_DIRECTORY = "cache-directory";
    private static final String HASHER_MARKER = "-HasheR-";

    private CachingFileSystemStorage storage;
    private Hasher hasher;
    private FileManager fileManager;

    @Before
    public void setup() {
        hasher = spy(new Hasher() {

            @Override
            public String hash(final String text) {
                return CachingFileSystemStorageTest.hash(text);
            }
        });
        fileManager = mock(FileManager.class);
        storage = spy(new CachingFileSystemStorage(CACHE_DIRECTORY, hasher, fileManager));
    }

    @Test
    public void hasRedirection_markerExists_returnsTrue() {
        final String path = "file.less";
        final File file = mock(File.class);
        when(file.exists()).thenReturn(Boolean.TRUE);
        doReturn(file).when(storage).createAbstractFile(path, "redirection.marker");

        final boolean result = storage.hasRedirection(path);

        assertThat(result).isTrue();
    }

    @Test
    public void hasRedirection_markerDoesNotExist_returnsFalse() {
        final String path = "file.less";
        final File file = mock(File.class);
        doReturn(file).when(storage).createAbstractFile(path, "redirection.marker");

        final boolean result = storage.hasRedirection(path);

        assertThat(result).isFalse();
    }

    @Test
    public void getRedirection_redirectionFileExists_returnsSavedPath() throws IOException {
        final String path = "file.less";
        final String savedPath = "saved.less";
        final File file = mock(File.class);
        when(file.exists()).thenReturn(Boolean.TRUE);
        doReturn(file).when(storage).createAbstractFile(path, "redirection.txt");
        when(fileManager.readString(file)).thenReturn(savedPath);

        final String result = storage.getRedirection(path);

        assertThat(result).isSameAs(savedPath);
    }

    @Test
    public void getRedirection_redirectionFileDoesNotExist_returnsInputPath() throws IOException {
        final String path = "file.less";
        final File file = mock(File.class);
        doReturn(file).when(storage).createAbstractFile(path, "redirection.txt");

        final String result = storage.getRedirection(path);

        assertThat(result).isSameAs(path);
    }

    @Test
    public void saveRedirection_redirectionPathIsTheSameAsPath_createsOnlyMarketFile() throws IOException {
        final String path = "direct.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "redirection.marker");

        storage.saveRedirection(path, path);

        verify(fileManager).createEmptyFile(marker);
        verifyNoMoreInteractions(fileManager);
    }

    @Test
    public void saveRedirection_redirectionPathIsDifferentThanInputPath_createsMarkerAndRedirectionFiles() throws IOException {
        final String path = "file.less";
        final String directPath = "direct.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "redirection.marker");
        final File redirection = mock(File.class);
        doReturn(redirection).when(storage).createAbstractFile(path, "redirection.txt");

        storage.saveRedirection(path, directPath);

        verify(fileManager).createEmptyFile(marker);
        verify(fileManager).writeString(redirection, directPath);
        verifyNoMoreInteractions(fileManager);
    }

    @Test
    public void hasExistent_markerExists_returnsTrue() {
        final String path = "file.less";
        final File marker = mock(File.class);
        when(marker.exists()).thenReturn(Boolean.TRUE);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");

        final boolean result = storage.hasExistent(path);

        assertThat(result).isTrue();
    }

    @Test
    public void hasExistent_markerDoesNotExistButContentExists_returnsTrue() {
        final String path = "file.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");
        final File content = mock(File.class);
        when(content.exists()).thenReturn(Boolean.TRUE);
        doReturn(content).when(storage).createAbstractFile(path, "content.bin");

        final boolean result = storage.hasExistent(path);

        assertThat(result).isTrue();
    }

    @Test
    public void hasExistent_markerAndContentDoNotExist_returnsFalse() {
        final String path = "file.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");
        final File content = mock(File.class);
        doReturn(content).when(storage).createAbstractFile(path, "content.bin");

        final boolean result = storage.hasExistent(path);

        assertThat(result).isFalse();
    }

    @Test
    public void isExistent_markerDoesNotExist_returnsTrue() {
        final String path = "file.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");

        final boolean result = storage.isExistent(path);

        assertThat(result).isTrue();
    }

    @Test
    public void isExistent_markerExists_returnsFalse() {
        final String path = "file.less";
        final File marker = mock(File.class);
        when(marker.exists()).thenReturn(Boolean.TRUE);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");

        final boolean result = storage.isExistent(path);

        assertThat(result).isFalse();
    }

    @Test
    public void saveAsNonExistent() throws IOException {
        final String path = "file.less";
        final File marker = mock(File.class);
        doReturn(marker).when(storage).createAbstractFile(path, "non-existence.marker");

        storage.saveAsNonExistent(path);

        verify(fileManager).createEmptyFile(marker);
        verifyNoMoreInteractions(fileManager);
    }

    @Test
    public void readDependencies_dependenciesFileDoesNotExist_returnsMutableEmptyCollection() throws IOException {
        final String path = "file.less";
        final File dependenciesFile = mock(File.class);
        doReturn(dependenciesFile).when(storage).createAbstractFile(path, "dependencies.txt");

        final Collection<String> result = storage.readDependencies(path);

        assertThat(result).isEmpty();
        result.add("test");
        assertThat(result).containsExactly("test");
    }

    @Test
    public void readDependencies_dependenciesFileExists_returnsMutableCollection() throws IOException {
        final String path = "file.less";
        final File dependenciesFile = mock(File.class);
        when(dependenciesFile.exists()).thenReturn(Boolean.TRUE);
        doReturn(dependenciesFile).when(storage).createAbstractFile(path, "dependencies.txt");
        final String dependency1 = "dept1.less";
        final String dependency2 = "dept2.less";
        when(fileManager.readLines(dependenciesFile)).thenReturn(Arrays.asList(dependency1, dependency2));

        final Collection<String> result = storage.readDependencies(path);

        assertThat(result).containsOnly(dependency1, dependency2);
        result.add("test");
        assertThat(result).containsOnly(dependency1, dependency2, "test");
    }

    @Test
    public void saveDependencies_savesSortedDependencies() throws IOException {
        final String path = "file.less";
        final Collection<String> dependencies = Arrays.asList("C.less", "A.less", "B.less");
        final File dependenciesFile = mock(File.class);
        doReturn(dependenciesFile).when(storage).createAbstractFile(path, "dependencies.txt");

        storage.saveDependencies(path, dependencies);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Collection<String>> sortedDependencies = ArgumentCaptor.forClass(Collection.class);
        verify(fileManager).writeLines(eq(dependenciesFile), sortedDependencies.capture());
        assertThat(sortedDependencies.getValue()).containsExactly("A.less", "B.less", "C.less");
    }

    @Test
    public void hasContent_contentExists_returnsTrue() {
        final String path = "file.less";
        final File contentFile = mock(File.class);
        when(contentFile.exists()).thenReturn(Boolean.TRUE);
        doReturn(contentFile).when(storage).createAbstractFile(path, "content.bin");

        final boolean result = storage.hasContent(path);

        assertThat(result).isTrue();
    }

    @Test
    public void hasContent_contentDoesNotExist_returnsFalse() {
        final String path = "file.less";
        final File contentFile = mock(File.class);
        doReturn(contentFile).when(storage).createAbstractFile(path, "content.bin");

        final boolean result = storage.hasContent(path);

        assertThat(result).isFalse();
    }

    @Test
    public void readContent() throws IOException {
        final String path = "file.less";
        final File contentFile = mock(File.class);
        doReturn(contentFile).when(storage).createAbstractFile(path, "content.bin");
        final byte[] data = new byte[] { 1, 2, 3 };
        when(fileManager.readByteArray(contentFile)).thenReturn(data);

        final byte[] result = storage.readContent(path);

        assertThat(result).isSameAs(data);
    }

    @Test
    public void saveContent() throws IOException {
        final String path = "file.less";
        final File contentFile = mock(File.class);
        doReturn(contentFile).when(storage).createAbstractFile(path, "content.bin");
        final byte[] data = new byte[] { 1, 2, 3 };

        storage.saveContent(path, data);

        verify(fileManager).writeByteArray(contentFile, data);
    }

    @Test
    public void readEncoding_fileExists_returnsSavedEncoding() throws IOException {
        final String path = "file.less";
        final File encodingFile = mock(File.class);
        when(encodingFile.exists()).thenReturn(Boolean.TRUE);
        doReturn(encodingFile).when(storage).createAbstractFile(path, "encoding.txt");
        final String encoding = "encoding";
        when(fileManager.readString(encodingFile)).thenReturn(encoding);

        final String result = storage.readEncoding(path);

        assertThat(result).isSameAs(encoding);
    }

    @Test
    public void readEncoding_fileDoesNotExist_returnsNull() throws IOException {
        final String path = "file.less";
        final File encodingFile = mock(File.class);
        doReturn(encodingFile).when(storage).createAbstractFile(path, "encoding.txt");

        final String result = storage.readEncoding(path);

        assertThat(result).isNull();
    }

    @Test
    public void saveEncoding_encodingIsNull_doesNothing() throws IOException {
        final String path = "file.less";
        final String encoding = null;

        storage.saveEncoding(path, encoding);

        verify(storage).saveEncoding(path, encoding);
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void saveEncoding_encodingIsNotNull_savesEncoding() throws IOException {
        final String path = "file.less";
        final String encoding = "encoding";
        final File encodingFile = mock(File.class);
        doReturn(encodingFile).when(storage).createAbstractFile(path, "encoding.txt");

        storage.saveEncoding(path, encoding);

        verify(fileManager).writeString(encodingFile, encoding);
    }

    @Test
    public void createAbstractFile_pathIsUsedForTheFirstTime_executesHasher() {
        final String path = "directory/file.less";
        final String filename = "file.txt";

        final File file = storage.createAbstractFile(path, filename);

        assertThat(file).isEqualTo(createFile(path, filename));
        assertThat(storage.idsCache).containsOnly(entry(path, hash(path)));
        verify(hasher).hash(path);
        verifyNoMoreInteractions(hasher);
    }

    @Test
    public void createAbstractFile_pathIsUsedMoreTimes_executesHasherOnlyOnce() {
        final String path = "directory/file.less";
        final String filename1 = "file1.txt";
        final String filename2 = "file1.txt";
        final String filename3 = "file1.txt";

        final File file1 = storage.createAbstractFile(path, filename1);
        final File file2 = storage.createAbstractFile(path, filename2);
        final File file3 = storage.createAbstractFile(path, filename3);

        assertThat(file1).isEqualTo(createFile(path, filename1));
        assertThat(file2).isEqualTo(createFile(path, filename2));
        assertThat(file3).isEqualTo(createFile(path, filename3));
        assertThat(storage.idsCache).containsOnly(entry(path, hash(path)));
        verify(hasher).hash(path);
        verifyNoMoreInteractions(hasher);
    }

    @Test
    public void createAbstractFile_morePaths_executesHasherOnlyOnceForEveryPath() {
        final String path1 = "directory/file1.less";
        final String path2 = "directory/file2.less";
        final String filename1 = "file1.txt";
        final String filename2 = "file2.txt";

        final File file11 = storage.createAbstractFile(path1, filename1);
        final File file12 = storage.createAbstractFile(path1, filename2);
        final File file21 = storage.createAbstractFile(path2, filename1);
        final File file22 = storage.createAbstractFile(path2, filename2);

        assertThat(file11).isEqualTo(createFile(path1, filename1));
        assertThat(file12).isEqualTo(createFile(path1, filename2));
        assertThat(file21).isEqualTo(createFile(path2, filename1));
        assertThat(file22).isEqualTo(createFile(path2, filename2));
        assertThat(storage.idsCache).containsOnly(entry(path1, hash(path1)), entry(path2, hash(path2)));
        verify(hasher).hash(path1);
        verify(hasher).hash(path2);
        verifyNoMoreInteractions(hasher);
    }

    private static File createFile(final String path, final String filename) {
        return new File(CACHE_DIRECTORY + File.separator + hash(path) + File.separator + filename);
    }

    private static String hash(final String path) {
        return HASHER_MARKER + new File(path).getName() + HASHER_MARKER;
    }
}
