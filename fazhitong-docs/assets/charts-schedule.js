(function () {
  var style = getComputedStyle(document.documentElement);
  var accent = style.getPropertyValue('--accent').trim();
  var accent2 = style.getPropertyValue('--accent2').trim();
  var muted = style.getPropertyValue('--muted').trim();
  var rule = style.getPropertyValue('--rule').trim();
  var ink = style.getPropertyValue('--ink').trim();

  var el = document.getElementById('chart-gantt');
  if (!el) return;

  var weeks = ['W1', 'W2', 'W3', 'W4', 'W5', 'W6', 'W7', 'W8', 'W9', 'W10', 'W11', 'W12'];

  // 每个任务：[名称, 开始周(0起), 结束周(0起, 含)]
  var tasks = [
    ['4.1 向量库搭建', 0, 2],
    ['2.4 文书数据补全', 0, 4],
    ['3.1 题库模型', 1, 3],
    ['1.1 审查数据模型', 2, 4],
    ['1.2 风险规则引擎', 3, 6],
    ['4.2 切片向量化', 3, 6],
    ['2.1 多维筛选接口', 4, 6],
    ['3.2 评分引擎', 4, 7],
    ['4.3 语义检索接口', 5, 8],
    ['1.3 大模型审查编排', 6, 9],
    ['2.2 争议焦点提炼', 7, 10],
    ['3.3 问卷交互页', 7, 10],
    ['4.4 咨询审查接入 RAG', 9, 11],
    ['1.4 审查结果页', 8, 11],
    ['3.4 报告生成', 9, 11],
    ['2.3 检索结果页', 8, 11],
    ['5.2 性能压测', 10, 11],
    ['5.3 部署验收', 11, 11]
  ];

  // 里程碑
  var milestones = [
    { name: 'M1 基础就绪', week: 2 },
    { name: 'M2 核心功能', week: 7 },
    { name: 'M3 集成完成', week: 9 },
    { name: 'M4 发布验收', week: 11 }
  ];

  var chart = echarts.init(el, null, { renderer: 'svg' });
  var categoryData = tasks.map(function (t) { return t[0]; }).reverse();
  var milestoneData = milestones.map(function (t) { return t[0]; }).reverse();

  var series = [];

  // 任务条形
  series.push({
    type: 'custom',
    renderItem: function (params, api) {
      var catIndex = api.value(0);
      var start = api.value(1);
      var end = api.value(2);
      var startCoord = api.coord([start, catIndex]);
      var endCoord = api.coord([end + 1, catIndex]);
      var height = api.size([0, 1])[1] * 0.45;
      var rectShape = echarts.graphic.clipRectByRect({
        x: startCoord[0],
        y: startCoord[1] - height / 2,
        width: Math.max(endCoord[0] - startCoord[0], 2),
        height: height
      }, { x: params.coordSys.x, y: params.coordSys.y, width: params.coordSys.width, height: params.coordSys.height });
      return rectShape && {
        type: 'rect',
        shape: rectShape,
        style: api.style({ fill: accent, opacity: 0.85 })
      };
    },
    encode: { x: [1, 2], y: 0 },
    data: tasks.map(function (t) { return [t[0], t[1], t[2]]; }),
    tooltip: {
      formatter: function (p) {
        return p.name + '<br>第 ' + (weeks[p.value[1]]) + ' 周 至 第 ' + (weeks[p.value[2]]) + ' 周';
      }
    }
  });

  // 里程碑菱形
  series.push({
    type: 'custom',
    renderItem: function (params, api) {
      var catIndex = api.value(0);
      var week = api.value(1);
      var coord = api.coord([week + 0.5, catIndex]);
      var r = api.size([0, 1])[1] * 0.28;
      var diamondShape = echarts.graphic.clipRectByRect({
        x: coord[0] - r,
        y: coord[1] - r,
        width: r * 2,
        height: r * 2
      }, { x: params.coordSys.x, y: params.coordSys.y, width: params.coordSys.width, height: params.coordSys.height });
      return diamondShape && {
        type: 'rect',
        shape: diamondShape,
        style: api.style({ fill: accent2 }),
        styleEmphasis: api.style({ fill: accent2 })
      };
    },
    encode: { x: [1], y: 0 },
    data: milestones.map(function (t) { return [t.name, t.week]; }),
    tooltip: { formatter: function (p) { return p.name; } }
  });

  chart.setOption({
    animation: false,
    grid: { left: 180, right: 40, top: 30, bottom: 40 },
    tooltip: { trigger: 'item', appendToBody: true },
    xAxis: {
      type: 'value',
      min: 0,
      max: 12,
      interval: 1,
      axisLabel: { color: muted, formatter: function (v) { return 'W' + (v + 1); } },
      splitLine: { lineStyle: { color: rule } },
      axisLine: { lineStyle: { color: rule } }
    },
    yAxis: {
      type: 'category',
      data: categoryData.concat(milestoneData),
      axisLabel: { color: ink, fontSize: 11 },
      axisLine: { lineStyle: { color: rule } },
      axisTick: { show: false }
    },
    series: series
  });

  window.addEventListener('resize', function () { chart.resize(); });
})();
