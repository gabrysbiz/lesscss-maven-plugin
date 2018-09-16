package biz.gabrys.maven.plugins.lesscss.io;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerTest {

    @Spy
    private FileManager fileManager;
    @Mock
    private File file;

    @Test
    public void createEmptyFile_fileDoesNotExist_success() throws IOException {
        when(file.createNewFile()).thenReturn(Boolean.TRUE);

        fileManager.createEmptyFile(file);

        verify(file).createNewFile();
    }

    @Test(expected = IOException.class)
    public void createEmptyFile_fileCannotBeCreated_throwsException() throws IOException {
        fileManager.createEmptyFile(file);
    }

    @Test
    public void delete_fileDoesNotExist_doesNothing() throws IOException {
        fileManager.delete(file);

        verify(file).exists();
        verifyNoMoreInteractions(file);
    }

    @Test
    public void delete_passExistingFile_executesDeleteFile() throws IOException {
        when(file.exists()).thenReturn(Boolean.TRUE);
        when(file.isFile()).thenReturn(Boolean.TRUE);
        doNothing().when(fileManager).deleteFile(file);

        fileManager.delete(file);

        verify(fileManager).delete(file);
        verify(fileManager).deleteFile(file);
        verifyNoMoreInteractions(fileManager);
    }

    @Test
    public void delete_passExistingDirectory_executesDeleteDirectory() throws IOException {
        when(file.exists()).thenReturn(Boolean.TRUE);
        when(file.isDirectory()).thenReturn(Boolean.TRUE);
        doNothing().when(fileManager).deleteDirectory(file);

        fileManager.delete(file);

        verify(fileManager).delete(file);
        verify(fileManager).deleteDirectory(file);
        verifyNoMoreInteractions(fileManager);
    }

    @Test(expected = IOException.class)
    public void delete_passExistingNonFileOrDirectory_throwsException() throws IOException {
        when(file.exists()).thenReturn(Boolean.TRUE);
        fileManager.delete(file);
    }

    @Test
    public void deleteFile_fileCanBeDeleted_success() throws IOException {
        when(file.delete()).thenReturn(Boolean.TRUE);

        fileManager.deleteFile(file);
    }

    @Test(expected = IOException.class)
    public void deleteFile_fileCannotBeDeleted_throwsException() throws IOException {
        fileManager.deleteFile(file);
    }
}
