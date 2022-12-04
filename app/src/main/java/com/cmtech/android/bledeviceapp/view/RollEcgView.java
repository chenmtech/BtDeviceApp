package com.cmtech.android.bledeviceapp.view;

import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_SYMBOL_DESCRIPTION_MAP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RollEcgView extends RollRecordView {
    private static final float SECOND_PER_GRID = 0.04f; // 横向每个栅格代表的秒数，对应于走纸速度
    private static final float MV_PER_GRID = 0.1f; // 纵向每个栅格代表的mV，对应于灵敏度
    private static final int PIXEL_PER_GRID = 10; // 每个栅格包含的像素个数

    private static final Paint ANN_PAINT = new Paint();

    public RollEcgView(Context context) {
        super(context);

        ANN_PAINT.setTextSize(40);
        ANN_PAINT.setColor(Color.WHITE);
        ANN_PAINT.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
    }

    public RollEcgView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ANN_PAINT.setTextSize(40);
        ANN_PAINT.setColor(Color.WHITE);
        ANN_PAINT.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
    }

    public void setup(BleEcgRecord record, float[] zeroLocs) {
        float[] mvPerGrid = new float[record.getChannelNum()];
        Arrays.fill(mvPerGrid, MV_PER_GRID);
        super.setup(record, zeroLocs, SECOND_PER_GRID, mvPerGrid, PIXEL_PER_GRID);
    }

    public void setup(BleEcgRecord record) {
        float[] mvPerGrid = new float[record.getChannelNum()];
        Arrays.fill(mvPerGrid, MV_PER_GRID);
        super.setup(record, SECOND_PER_GRID, mvPerGrid, PIXEL_PER_GRID);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(record == null) return;

        int beginPos = curPos - dataNumInView + 1;

        List<Integer> annPoses = ((BleEcgRecord)record).getAnnPoses();
        List<String> annSymbols = ((BleEcgRecord)record).getAnnSymbols();
        for(int i = 0; i < annPoses.size(); i++) {
            int pos = annPoses.get(i);
            String symbol = annSymbols.get(i);
            if(pos < beginPos) continue;
            if(pos <= curPos) {
                if(symbol.startsWith("+(")) {
                    int x = (pos - beginPos) * pixelPerData;
                    canvas.drawLine(x,0,x,viewHeight, ANN_PAINT);
                    canvas.drawText(Objects.requireNonNull(ANN_SYMBOL_DESCRIPTION_MAP.get(symbol)), x, viewHeight-10, ANN_PAINT);
                }
            } else {
                break;
            }
        }
    }
}
