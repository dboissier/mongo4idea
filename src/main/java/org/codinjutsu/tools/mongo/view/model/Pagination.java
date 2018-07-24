/*
 * Copyright (c) 2018 David Boissier.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.view.model;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Collection;

public class Pagination {

    private NbPerPage nbPerPage;
    private int pageNumber;
    private int totalDocuments;
    @NotNull
    private final Collection<Runnable> mySetFilterListeners = ContainerUtil.newArrayList();


    public Pagination() {
        this.pageNumber = 1;
        this.nbPerPage = NbPerPage.ALL;
        this.totalDocuments = 0;
    }

    public void next() {
        setPageNumber(getPageNumber() + 1);
    }

    public void previous() {
        setPageNumber(getPageNumber() - 1);
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getNbDocumentsPerPage() {
        return nbPerPage.nb;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getStartIndex() {
        if (NbPerPage.ALL.equals(nbPerPage)) {
            return 0;
        } else {
            return getNbDocumentsPerPage() * (getPageNumber() - 1);
        }
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        for (Runnable listener : mySetFilterListeners) {
            listener.run();
        }
    }

    public NbPerPage getNbPerPage() {
        return nbPerPage;
    }

    public void setNbPerPage(NbPerPage nbPerPage) {
        this.nbPerPage = nbPerPage;
        this.pageNumber = 1;
        for (Runnable listener : mySetFilterListeners) {
            listener.run();
        }
    }

    public void addSetPageListener(@NotNull Runnable runnable) {
        mySetFilterListeners.add(runnable);
    }

    public int getTotalPageNumber() {
        if (getNbDocumentsPerPage() == 0) {
            return 1;
        }
        return new BigDecimal(totalDocuments)
                .divide(new BigDecimal(getNbDocumentsPerPage()), BigDecimal.ROUND_CEILING)
                .toBigInteger()
                .intValue();
    }
}
