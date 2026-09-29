package com.agentForgeBackend.shared.defaultImplements;

import com.agentForgeBackend.shared.defaultInterfaces.DefaultService;
import com.agentForgeBackend.shared.query.PageableRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultControllerListEndpointTest {

    @Test
    void getListPageIsMappedToPostList() throws Exception {
        Method method = DefaultController.class.getMethod("getListPage", PageableRequest.class);

        PostMapping postMapping = method.getAnnotation(PostMapping.class);

        assertArrayEquals(new String[]{"/list"}, postMapping.value());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getListPageDelegatesToDefaultService() throws Exception {
        DefaultService<TestDTO, TestMiniDTO, TestListDTO, TestForm, Long> service = mock(DefaultService.class);
        TestController controller = new TestController(service);
        PageableRequest request = new PageableRequest();
        Page<TestListDTO> page = new PageImpl<>(List.of(new TestListDTO(1L, "ada")));

        when(service.getListPage(request)).thenReturn(page);

        ResponseEntity<Page<TestListDTO>> response = controller.getListPage(request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertSame(page, response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(service).getListPage(request);
    }

    private static final class TestController extends DefaultController<
            TestDTO,
            TestMiniDTO,
            TestListDTO,
            TestForm,
            Long> {

        private TestController(DefaultService<TestDTO, TestMiniDTO, TestListDTO, TestForm, Long> defaultService) {
            super(defaultService);
        }
    }

    private record TestDTO(Long id) {
    }

    private record TestMiniDTO(Long id) {
    }

    private record TestListDTO(Long id, String name) {
    }

    private record TestForm(String name) {
    }
}
