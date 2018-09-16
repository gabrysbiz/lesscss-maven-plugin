package biz.gabrys.maven.plugins.lesscss.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedHttpFileSystemTest {

    @Spy
    private ExtendedHttpFileSystem2 fileSystem;
    @Mock
    private HttpURLConnection connection;

    @Before
    public void setup() throws IOException {
        doReturn(connection).when(fileSystem).makeConnection(any(URL.class), eq(false));
    }

    @Test
    public void getLastModified_fileDoesNotExist_returnsNull() throws IOException {
        final String path = "http://example.org/file.less";
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);

        final Date result = fileSystem.getLastModified(path);

        assertThat(result).isNull();
        verify(fileSystem).disconnect(connection);
    }

    @Test
    public void getLastModified_fileExists_lastModifiedIsUnknown_returnsNull() throws IOException {
        final String path = "http://example.org/file.less";
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getLastModified()).thenReturn(0L);

        final Date result = fileSystem.getLastModified(path);

        assertThat(result).isNull();
        verify(fileSystem).disconnect(connection);
    }

    @Test
    public void getLastModified_fileExists_lastModifiedIsKnown_returnsDate() throws IOException {
        final String path = "http://example.org/file.less";
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        final Date date = new Date();
        when(connection.getLastModified()).thenReturn(date.getTime());

        final Date result = fileSystem.getLastModified(path);

        assertThat(result).isEqualTo(date);
        verify(fileSystem).disconnect(connection);
    }

    private static class ExtendedHttpFileSystem2 extends ExtendedHttpFileSystem {

        @Override
        protected HttpURLConnection makeConnection(final URL url, final boolean fetchResponseBody) throws IOException {
            // allow stubs in tests
            throw new UnsupportedOperationException();
        }

        @Override
        protected void disconnect(final HttpURLConnection connection) {
            // allow stubs in tests
        }
    }
}
