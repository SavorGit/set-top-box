package tv.danmaku.ijk.media.exo2;

import android.util.Log;

import java.util.ArrayList;

public class RangeManager {
    private ArrayList<Range> wroteRanges = new ArrayList<>();
    private String fileTag;

    RangeManager(String fileTag) {
        this.fileTag = fileTag;
    }

    public synchronized void reset() {
        wroteRanges.clear();
    }

    public synchronized void recordNewPiece(long start, long len) {
        Log.d("ZHQ", "recordNewPiece with start=" + start + " len=" + len);
        // 按大小顺序将新Range插入到集合中
        Range newRange = new Range(start, start + len);
        int position = 0;
        for (int i = 0; i < wroteRanges.size(); i++) {
            Range range = wroteRanges.get(i);
            if (newRange.end <= range.start) {
                position = i - 1;
                break;
            } else if (newRange.start >= range.end) {
                position = i + 1;
                break;
            }
        }
        if (position < 0) {
            position = 0;
        }
        if (position >= wroteRanges.size()) {
            wroteRanges.add(newRange);
        } else {
            wroteRanges.add(position, newRange);
        }
        Log.d("ZHQ", "after insert, wroteRanges.size=" + wroteRanges.size());


        if (wroteRanges.size() > 1) {
            // 合并有交集的Range
            Log.d("ZHQ", "before merge, wroteRanges.size=" + wroteRanges.size());
            ArrayList<Range> newRanges = new ArrayList<>();
            for (int i = 0; i < wroteRanges.size();) {
                Range range = wroteRanges.get(i);

                if (i + 1 < wroteRanges.size()) {

                    Log.d("ZHQ", "will push in newRanges, current size is " + newRanges.size());
                    newRanges.add(range);

                    while (i + 1 < wroteRanges.size()) {
                        Range rangeNext = wroteRanges.get(i + 1);
                        i++;
                        if (range.end >= rangeNext.start) {
                            // 合并下一个range
                            range.end = rangeNext.end;
                        } else {
                            break;
                        }
                    }
                    // 已到达当前可合并的极限
                } else {

                    if (wroteRanges.get(i - 1).end < range.start) {
                        Log.d("ZHQ", "will push in newRanges, current size is " + newRanges.size());
                        newRanges.add(range);
                    }

                    // 已到达集合末尾
                    i++;

//                    Log.d("ZHQ", "will push in newRanges, current size is " + newRanges.size());
//                    newRanges.add(range);
                }
            }
            wroteRanges.clear();
            wroteRanges = newRanges;

            Log.d("ZHQ", "after merge, wroteRanges.size=" + wroteRanges.size());
            for (Range range :
                    wroteRanges) {
                Log.d("ZHQ", range + "");
            }
        }
    }

/*
    public synchronized void recordNewPiece(long start, long len) {
        Log.d("ZHQ", "recordNewPiece with start=" + start + " len=" + len);
        // 按大小顺序将新Range插入到集合中
        Range newRange = new Range(start, start + len);
        int position = 0;
        for (int i = 0; i < wroteRanges.size(); i++) {
            Range range = wroteRanges.get(i);
            if (newRange.end <= range.start) {
                position = i - 1;
                break;
            } else if (newRange.start >= range.end) {
                position = i + 1;
                break;
            }
        }
        if (position < 0) {
            position = 0;
        }
        if (position >= wroteRanges.size()) {
            wroteRanges.add(newRange);
        } else {
            wroteRanges.add(position, newRange);
        }
        Log.d("ZHQ", "after insert, wroteRanges.size=" + wroteRanges.size());


        if (wroteRanges.size() > 1) {
            // 合并有交集的Range
            Log.d("ZHQ", "before merge, wroteRanges.size=" + wroteRanges.size());
            ArrayList<Range> newRanges = new ArrayList<>();
            for (int i = 0; i < wroteRanges.size();) {
                Range range = wroteRanges.get(i);

                Log.d("ZHQ", "will push in newRanges, current size is " + newRanges.size());
                newRanges.add(range);

                if (i + 1 < wroteRanges.size()) {
                    while (i + 1 < wroteRanges.size()) {
                        Range rangeNext = wroteRanges.get(i + 1);
                        i++;
                        if (range.end >= rangeNext.start) {
                            // 合并下一个range
                            range.end = rangeNext.end;
                        } else {
                            break;
                        }
                    }
                    // 已到达当前可合并的极限
                } else {
                    // 已到达集合末尾
                    i++;

//                    Log.d("ZHQ", "will push in newRanges, current size is " + newRanges.size());
//                    newRanges.add(range);
                }
            }
            wroteRanges.clear();
            wroteRanges = newRanges;

            Log.d("ZHQ", "after merge, wroteRanges.size=" + wroteRanges.size());
            for (Range range :
                    wroteRanges) {
                Log.d("ZHQ", range + "");
            }
        }
    }
*/

    public synchronized boolean hasWroteRange(long start, int len) {
        boolean re = false;
        for (Range range :wroteRanges) {
            if (range.start <= start && range.end >= start + len) {
                re = true;
                break;
            }
        }
        return re;
    }

    class Range {
        // [start, end)
        long start;
        long end;

        public Range(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}