package com.webank.offline.livedetection.util;

import java.util.LinkedList;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.util
 * @ClassName: FloatQueue
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/4/2 19:31
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/2 19:31
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FloatQueue {

    private LinkedList<float[]> list = new LinkedList();
    private int maxSize;

    public FloatQueue(int size) {
        this.maxSize = size;
    }

    // 销毁队列
    public void clear() {
        list.clear();
    }

    // 判断队列是否为空
    public boolean queueEmpty() {
        return list.isEmpty();
    }

    // 进队
    public void enQueue(float[] o) {
        if (queueLength() == maxSize) {
            deQueue();
        }
        list.addLast(o);
    }

    // 出队
    public void deQueue() {
        if (!list.isEmpty()) {
            list.removeFirst();
        }
    }

    // 获取队列长度
    public int queueLength() {
        return list.size();
    }

    // 查看队首元素
    public Object queuePeek() {
        return list.getFirst();
    }

    public float[] average(float[] point) {
        enQueue(point);
        float[] ave = new float[point.length];
        for (int i = 0; i < ave.length; i++) {
            float total = 0;
            for (float[] item : list) {
                total += item[i];
            }
            ave[i] = total / 3;
        }
        return ave;
    }

}
