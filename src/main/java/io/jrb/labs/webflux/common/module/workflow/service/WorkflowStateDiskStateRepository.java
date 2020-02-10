/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.webflux.common.module.workflow.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
public class WorkflowStateDiskStateRepository implements IWorkflowStateRepository {

    private final Path baseDirectory;

    public WorkflowStateDiskStateRepository(final String baseDirectory) {
        this.baseDirectory = Paths.get(baseDirectory);
    }

    @Override
    public void delete(final String key) {
        final File stateFile = getStateFile(key);
        try {
            stateFile.delete();
        } catch(final Exception e) {
            log.error("Unable to delete workflow state (" + key + ")!", e);
        }
    }

    @Override
    public <C extends IWorkflowContext> Optional<C> find(final String key, final Class<C> contextClass) {
        final File stateFile = getStateFile(key);
        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFile))) {
            final C context = contextClass.cast(ois.readObject());
            return Optional.ofNullable(context);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(final String key, final IWorkflowContext context) {
        final File stateFile = getStateFile(key);
        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(stateFile))) {
            oos.writeObject(context);
        } catch(final Exception e) {
            log.error("Unable to save workflow state (" + key + ")!", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private File getStateFile(final String key) {
        final Path stateFilePath = baseDirectory.resolve(key + "_state.raw");
        final File stateFile = new File(stateFilePath.toUri());
        stateFile.deleteOnExit();
        return stateFile;
    }

}
