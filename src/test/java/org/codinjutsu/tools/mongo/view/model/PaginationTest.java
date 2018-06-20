package org.codinjutsu.tools.mongo.view.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationTest {

    @Test
    public void shouldStartAt0WhenPageNumberIs1() {
        Pagination pagination = new Pagination();
        pagination.setNbPerPage(NbPerPage.FIFTY);
        assertThat(pagination.getStartIndex()).isEqualTo(0);
    }

    @Test
    public void shouldStartAt10WhenPageNumberIs2AndNbDocumentPerPageIs10() {
        Pagination pagination = new Pagination();
        pagination.setNbPerPage(NbPerPage.TEN);
        pagination.setPageNumber(2);
        assertThat(pagination.getStartIndex()).isEqualTo(10);
    }

    @Test
    public void shouldStartAt20WhenPageNumberIs3AndNbDocumentPerPageIs10() {
        Pagination pagination = new Pagination();
        pagination.setNbPerPage(NbPerPage.TEN);
        pagination.setPageNumber(3);
        assertThat(pagination.getStartIndex()).isEqualTo(20);
    }

    @Test
    public void getDefaultTotalPageNumber() {
        Pagination pagination = new Pagination();
        assertThat(pagination.getTotalPageNumber()).isEqualTo(1);
    }
    @Test
    public void getTotalPageNumber() {
        Pagination pagination = new Pagination();
        pagination.setTotalDocuments(300);
        pagination.setNbPerPage(NbPerPage.TEN);
        assertThat(pagination.getTotalPageNumber()).isEqualTo(30);
    }
}