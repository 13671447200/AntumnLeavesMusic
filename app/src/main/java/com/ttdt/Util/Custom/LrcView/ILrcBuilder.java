package com.ttdt.Util.Custom.LrcView;


import com.ttdt.Util.Custom.LrcView.impl.LrcRow;

import java.util.List;

/**
 * 解析歌词，得到LrcRow的集合
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}
