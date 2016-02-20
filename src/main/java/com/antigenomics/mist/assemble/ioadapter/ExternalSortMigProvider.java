/*
 * Copyright 2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mist.assemble.ioadapter;

import com.antigenomics.mist.assemble.Mig;
import com.antigenomics.mist.misc.HeaderUtil;
import com.antigenomics.mist.umi.UmiTag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReader;
import org.geirove.exmeso.CloseableIterator;
import org.geirove.exmeso.ExternalMergeSort;
import org.geirove.exmeso.kryo.KryoSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ExternalSortMigProvider<S extends SequenceRead> implements MigProvider<S> {
    private static final int MAX_OPEN_FILES = 10;
    private final CloseableIterator<SequenceRead> sortedReads;
    private S lastRead = null;
    private UmiTag lastTag = null;

    public ExternalSortMigProvider(SequenceReader<S> reader) throws IOException {
        this(reader, File.createTempFile("mist_reads_" + UUID.randomUUID().toString(), ".bin"), 100000);
    }

    public ExternalSortMigProvider(SequenceReader<S> reader,
                                   File tempFile, int chunkSize) throws IOException {
        // Serialize to temp file using Kryo
        Kryo kryo = new Kryo();

        Output output = new Output(new FileOutputStream(tempFile));

        S read;

        while ((read = reader.take()) != null) {
            kryo.writeObject(output, read);
        }

        output.close();

        // Create comparator
        Comparator<SequenceRead> comparator = (o1, o2) -> {
            UmiTag tag1 =
                    HeaderUtil.parsedHeader(o1.getRead(0).getDescription()).toUmiTag(),
                    tag2 =
                            HeaderUtil.parsedHeader(o2.getRead(0).getDescription()).toUmiTag();
            return tag1.compareTo(tag2);
        };


        // Create the external merge sort instance
        KryoSerializer<SequenceRead> serializer = new KryoSerializer<>(SequenceRead.class);
        ExternalMergeSort<SequenceRead> sort = ExternalMergeSort.newSorter(serializer, comparator)
                .withChunkSize(chunkSize)
                .withMaxOpenFiles(MAX_OPEN_FILES)
                .withDistinct(true)
                .withCleanup(true)
                .withTempDirectory(tempFile.getParentFile())
                .build();

        // Read input file as an input stream and write sorted chunks.
        List<File> sortedChunks;
        try (InputStream input = new FileInputStream(tempFile)) {
            sortedChunks = sort.writeSortedChunks(serializer.readValues(input));
        }

        // Get a merge iterator over the sorted chunks. This will return the
        // objects in sorted order. Note that the sorted chunks will be deleted 
        // when the CloseableIterator is closed if 'cleanup' is set to true.
        sortedReads = sort.mergeSortedChunks(sortedChunks);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mig<S> take() {
        List<S> reads = new ArrayList<>();

        if (lastRead != null) {
            reads.add(lastRead);
        }

        while (sortedReads.hasNext()) {
            lastRead = (S) sortedReads.next();
            UmiTag tag = HeaderUtil.parsedHeader(lastRead.getRead(0).getDescription()).toUmiTag();
            boolean sameTag = lastTag == null || tag.equals(lastTag);
            lastTag = tag;
            if (sameTag) {
                reads.add(lastRead);
            } else {
                break;
            }
        }

        if (reads.isEmpty()) {
            try {
                sortedReads.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return new Mig<>(lastTag, reads);
        }
    }
}
