/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.profilers.cpu;

import com.android.tools.adtui.Choreographer;
import com.android.tools.adtui.chart.StateChart;
import com.android.tools.adtui.chart.linechart.LineChart;
import com.android.tools.adtui.chart.linechart.LineConfig;
import com.android.tools.adtui.model.Range;
import com.android.tools.adtui.model.RangedContinuousSeries;
import com.android.tools.profiler.proto.CpuProfiler;
import com.android.tools.profilers.ProfilerColors;
import com.android.tools.profilers.StageView;
import com.android.tools.profilers.StudioProfilers;
import com.android.tools.profilers.event.EventMonitor;
import com.android.tools.profilers.event.EventMonitorView;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CpuProfilerStageView extends StageView {

  private final JPanel myComponent;
  private final JBList myThreadsList;

  public CpuProfilerStageView(@NotNull CpuProfilerStage stage) {
    super(stage);

    StudioProfilers profilers = stage.getStudioProfilers();
    EventMonitor events = new EventMonitor(profilers);
    EventMonitorView eventsView = new EventMonitorView(events);

    CpuMonitor cpu = new CpuMonitor(profilers);

    myComponent = new JPanel(new GridBagLayout());
    myComponent.setBackground(ProfilerColors.MONITOR_BACKGROUND);
    Choreographer choreographer = new Choreographer(myComponent);

    JComponent eventsComponent = eventsView.initialize(choreographer);

    Range leftYRange = new Range(0, 100);
    LineChart lineChart = new LineChart();
    lineChart.addLine(new RangedContinuousSeries("App", stage.getStudioProfilers().getViewRange(), leftYRange, cpu.getThisProcessCpuUsage()),
                      new LineConfig(ProfilerColors.CPU_USAGE).setFilled(true).setStacked(true));
    lineChart.addLine(new RangedContinuousSeries("Others", stage.getStudioProfilers().getViewRange(), leftYRange, cpu.getOtherProcessesCpuUsage()),
                      new LineConfig(ProfilerColors.CPU_OTHER_USAGE).setFilled(true).setStacked(true));

    RangedListModel<CpuThreadsModel.RangedCpuThread> model = cpu.getThreadStates();
    myThreadsList = new JBList(model);
    myThreadsList.setCellRenderer(new ThreadCellRenderer(choreographer, myThreadsList));

    RangedList rangedList = new RangedList(profilers.getViewRange(), model);

    // TODO: Event monitor should be fixed size.
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 0.2;
    myComponent.add(eventsComponent, c);
    c.gridy = 1;
    c.weighty = 0.4;
    myComponent.add(lineChart, c);
    choreographer.register(lineChart);
    c.gridy = 2;
    c.weighty = 0.4;
    myComponent.add(myThreadsList, c);
    choreographer.register(rangedList);
  }


  private static class ThreadCellRenderer implements ListCellRenderer<CpuThreadsModel.RangedCpuThread> {

    private final JLabel myLabel;

    private AnimatedListRenderer<CpuThreadsModel.RangedCpuThread, StateChart<CpuProfiler.GetThreadsResponse.State>> myStateCharts;

    /**
     * Keep the index of the item currently hovered.
     */
    private int myHoveredIndex = -1;

    public ThreadCellRenderer(Choreographer choreographer, JList<CpuThreadsModel.RangedCpuThread> list) {
      myLabel = new JLabel();
      myLabel.setFont(myLabel.getFont().deriveFont(12.0f));
      myStateCharts = new AnimatedListRenderer<>(choreographer, list, thread -> {
        StateChart<CpuProfiler.GetThreadsResponse.State> chart = new StateChart<>(ProfilerColors.THREAD_STATES);
        chart.setHeightGap(0.35f);
        chart.addSeries(thread.getDataSeries());
        return chart;
      });
      list.addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          Point p = new Point(e.getX(), e.getY());
          myHoveredIndex = list.locationToIndex(p);
        }
      });
    }

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  CpuThreadsModel.RangedCpuThread value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      JLayeredPane panel = new JLayeredPane();
      panel.setLayout(new GridBagLayout());
      panel.setOpaque(true);
      myLabel.setText(value.getName());

      Color cellBackground = ProfilerColors.MONITOR_BACKGROUND;
      if (isSelected) {
        cellBackground = ProfilerColors.THREAD_SELECTED_BACKGROUND;
      } else if (myHoveredIndex == index) {
        cellBackground = ProfilerColors.THREAD_HOVER_BACKGROUND;
      }
      panel.setBackground(cellBackground);

      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0;
      c.weighty = 1.0;

      panel.add(myLabel, c);
      myLabel.setOpaque(false);
      panel.add(myStateCharts.get(index), c);
      return panel;
    }
  }

  @Override
  public JComponent getComponent() {
    return myComponent;
  }

  @Override
  public JComponent getToolbar() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel toolbar = new JPanel();

    JButton button = new JButton("<-");
    button.addActionListener(action -> returnToStudioStage());
    toolbar.add(button);

    JButton capture = new JButton("Capture");
    toolbar.add(capture);

    panel.add(toolbar, BorderLayout.WEST);
    return panel;
  }
}
