package de.is24.infrastructure.gridfs.http.web.controller;

import de.is24.infrastructure.gridfs.http.gridfs.GridFsService;
import de.is24.infrastructure.gridfs.http.repos.RepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class RepositoryControllerTest extends AbstractControllerTest {
  private static final String REPONAME = "someRepo-18.0.346357";

  private static final MockHttpServletRequestBuilder MOCK_DELETE_REQUEST = MockMvcRequestBuilders.delete("/repo/" +
    REPONAME);

  @Test
  public void deleteIsDelegated() throws Exception {
    mockMvc.perform(MOCK_DELETE_REQUEST).andExpect(status().isNoContent());

    verify(gridFsService).deleteRepository(REPONAME);
  }


  @Test
  public void deleteIsDelegatedForReposOnly() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.delete("/repo/arepo/some.file")).andExpect(status().isNotFound());

    verifyZeroInteractions(gridFsService);
  }
}
