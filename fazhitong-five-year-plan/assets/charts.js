(function () {
  var style = getComputedStyle(document.documentElement);
  var accent = style.getPropertyValue('--accent').trim();
  var accent2 = style.getPropertyValue('--accent2').trim();
  var ink = style.getPropertyValue('--ink').trim();
  var muted = style.getPropertyValue('--muted').trim();
  var rule = style.getPropertyValue('--rule').trim();
  var bg2 = style.getPropertyValue('--bg2').trim();

  // Mermaid 初始化
  if (window.mermaid) {
    mermaid.initialize({ startOnLoad: true, theme: 'neutral', securityLevel: 'loose' });
  }

  // --- 图 1：全球法律 AI 市场规模 ---
  var el1 = document.getElementById('chart-market');
  if (el1) {
    var c1 = echarts.init(el1, null, { renderer: 'svg' });
    c1.setOption({
      animation: false,
      grid: { left: 60, right: 30, top: 40, bottom: 40 },
      tooltip: {
        trigger: 'axis',
        appendToBody: true,
        valueFormatter: function (v) { return v + ' 亿美元'; }
      },
      xAxis: {
        type: 'category',
        data: ['2024', '2025', '2026', '2027', '2028'],
        axisLine: { lineStyle: { color: rule } },
        axisLabel: { color: muted }
      },
      yAxis: {
        type: 'value',
        name: '亿美元',
        nameTextStyle: { color: muted },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: rule } },
        axisLabel: { color: muted }
      },
      series: [{
        type: 'bar',
        barWidth: '46%',
        data: [
          { value: 37.5, itemStyle: { color: accent + '66' }, label: { show: true, position: 'top', color: muted, formatter: '37.5' } },
          { value: 45.9, itemStyle: { color: accent }, label: { show: true, position: 'top', color: accent, fontWeight: 700, formatter: '45.9' } },
          { value: 55.9, itemStyle: { color: accent }, label: { show: true, position: 'top', color: accent, fontWeight: 700, formatter: '55.9' } },
          { value: 68.4, itemStyle: { color: accent2 }, label: { show: true, position: 'top', color: accent2, formatter: '68.4' } },
          { value: 83.7, itemStyle: { color: accent2 }, label: { show: true, position: 'top', color: accent2, formatter: '83.7' } }
        ]
      }]
    });
    window.addEventListener('resize', function () { c1.resize(); });
  }

  // --- 图 2：八大核心能力成熟度雷达 ---
  var el2 = document.getElementById('chart-radar');
  if (el2) {
    var c2 = echarts.init(el2, null, { renderer: 'svg' });
    c2.setOption({
      animation: false,
      tooltip: { trigger: 'item', appendToBody: true },
      legend: { bottom: 0, textStyle: { color: muted }, icon: 'circle', itemWidth: 8, itemHeight: 8 },
      radar: {
        indicator: [
          { name: '智能咨询', max: 5 },
          { name: '文书生成', max: 5 },
          { name: '合同审查', max: 5 },
          { name: '类案检索', max: 5 },
          { name: '诉讼智能', max: 5 },
          { name: '企业法务', max: 5 },
          { name: '律所协作', max: 5 },
          { name: 'AI 数据底座', max: 5 }
        ],
        radius: '65%',
        splitNumber: 5,
        axisName: { color: ink, fontSize: 12 },
        splitArea: { areaStyle: { color: ['#fbfcfe', '#f3f6fb'] } },
        splitLine: { lineStyle: { color: rule } },
        axisLine: { lineStyle: { color: rule } }
      },
      series: [{
        type: 'radar',
        data: [
          { name: '现状', value: [3, 4, 1, 1, 0.5, 2, 1, 1.5], areaStyle: { color: accent + '33' }, lineStyle: { color: accent, width: 2 }, itemStyle: { color: accent } },
          { name: '五年目标', value: [5, 5, 5, 5, 4, 5, 4, 5], areaStyle: { color: accent2 + '33' }, lineStyle: { color: accent2, width: 2 }, itemStyle: { color: accent2 } }
        ]
      }]
    });
    window.addEventListener('resize', function () { c2.resize(); });
  }
})();
