/*
 * Copyright (c) 2018 Shared Libs (https://github.com/sharedlibs)
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.sharedlibs.stopwatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Stopwatch {

    private static ThreadLocal<List<Split>> sharedSplits = new ThreadLocal();

    private static long NULL = -1;

    private List<Split> splits = new ArrayList();

    private long startTime;

    private long splitTime;

    private long pauseTime;

    private Stopwatch() {
    }

    private static long now() {
        return Instant.now().toEpochMilli();
    }

    public static Stopwatch start() {
        return new Stopwatch().restart();
    }

    private static List<Split> getSharedSplits() {
        if (sharedSplits.get() == null) {
            synchronized (Stopwatch.class) {
                if (sharedSplits.get() == null) {
                    initSharedSplits();
                }
            }
        }

        return sharedSplits.get();
    }

    private static void initSharedSplits() {
        sharedSplits.set(new ArrayList());
    }

    public static void printSharedSplits() {
        printSplits(getSharedSplits());
    }

    private static void printSplits(List<Split> splits) {
        System.out.print(splits);
    }

    public static List<Split> sharedSplits() {
        return Collections.unmodifiableList(getSharedSplits());
    }

    public void printSplits() {
        printSplits(getSplits());
    }

    public long elapsed() {
        return (pauseTime == NULL ? now() : pauseTime) - startTime;
    }

    public long split(String label) throws StopwatchException {
        long now = now();

        if (pauseTime != NULL) {
            throw new StopwatchException("Stopwatch is paused");
        }

        long elapsed = now - (splitTime != NULL ? splitTime : startTime);
        splitTime = now;
        addSplit(new Split(label, elapsed));

        return elapsed;
    }

    public Stopwatch pause() {
        if (isRunning()) {
            pauseTime = now();
        }

        return this;
    }

    public Stopwatch resume() {
        if (!isRunning()) {
            long now = now();
            long stopped = now - pauseTime;

            startTime += stopped;
            splitTime = (splitTime == NULL ? NULL : splitTime + stopped);
            pauseTime = NULL;
        }

        return this;
    }

    public Stopwatch clear() {
        getSharedSplits().removeAll(getSplits());
        getSplits().clear();

        return this;
    }

    public Stopwatch restart() {
        clear();

        startTime = now();
        splitTime = NULL;
        pauseTime = NULL;

        return this;
    }

    public boolean isRunning() {
        return pauseTime == NULL;
    }

    public List<Split> splits() {
        return Collections.unmodifiableList(getSplits());
    }

    private List<Split> getSplits() {
        return splits;
    }

    private void addSplit(Split split) {
        getSplits().add(split);
        getSharedSplits().add(split);
    }
}
