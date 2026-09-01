<template>
  <div class="kg-page">
    <div class="page-header">
      <h2>法律知识图谱</h2>
      <p>以图谱形式呈现法律领域、法律法规与法律概念之间的关联关系</p>
    </div>

    <div class="toolbar">
      <div class="domain-tabs">
        <el-tag
          :type="activeDomain === '' ? 'primary' : 'info'"
          effect="plain"
          class="domain-tag"
          @click="switchDomain('')"
        >全部</el-tag>
        <el-tag
          v-for="d in domains"
          :key="d"
          :type="activeDomain === d ? 'primary' : 'info'"
          effect="plain"
          class="domain-tag"
          @click="switchDomain(d)"
        >{{ d }}</el-tag>
      </div>
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索法规或概念，如：合同"
          clearable
          style="width: 240px"
          @keyup.enter="searchRelated"
          @clear="clearSearch"
        >
          <template #append>
            <el-button @click="searchRelated">检索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="legend">
      <span class="legend-item"><i class="dot dot-domain"></i>法律领域</span>
      <span class="legend-item"><i class="dot dot-reg"></i>法律法规</span>
      <span class="legend-item"><i class="dot dot-concept"></i>法律概念</span>
      <span class="legend-tip">· 拖拽节点调整位置 · 点击节点高亮关联关系</span>
    </div>

    <div class="graph-card" v-loading="loading">
      <div v-if="!loading && nodes.length === 0" class="empty">
        <el-empty description="暂无图谱数据" />
      </div>
      <svg
        v-else
        ref="svgRef"
        :viewBox="`0 0 ${width} ${height}`"
        class="kg-svg"
        preserveAspectRatio="xMidYMid meet"
      >
        <!-- 边 -->
        <g>
          <line
            v-for="(l, i) in layout.links"
            :key="'l' + i"
            :x1="l.x1" :y1="l.y1" :x2="l.x2" :y2="l.y2"
            :class="['edge', { 'edge-hl': isHighlightLink(l) }]"
          />
        </g>
        <!-- 节点 -->
        <g>
          <g
            v-for="n in layout.nodes"
            :key="n.id"
            class="node"
            :transform="`translate(${n.x},${n.y})`"
            @mousedown="startDrag(n, $event)"
            @click="selectNode(n)"
          >
            <circle
              :r="nodeRadius(n)"
              :class="['node-circle', 'cat' + n.category, { 'node-hl': isHighlightNode(n.id) }]"
            />
            <text
              :y="nodeRadius(n) + 14"
              text-anchor="middle"
              :class="['node-label', { 'node-label-hl': isHighlightNode(n.id) }]"
            >{{ n.name }}</text>
          </g>
        </g>
      </svg>
    </div>

    <!-- 检索结果 -->
    <div v-if="relatedResult" class="related-panel">
      <div class="related-header">
        <strong>「{{ keyword }}」关联结果</strong>
        <el-button link type="primary" @click="clearSearch">关闭</el-button>
      </div>
      <div v-if="relatedResult.regulations && relatedResult.regulations.length" class="related-group">
        <div class="group-title">相关法规</div>
        <div v-for="r in relatedResult.regulations" :key="r.id" class="related-item">
          <span class="related-name">{{ r.title }}</span>
          <el-tag size="small" type="info" effect="plain">{{ r.domain || r.lawType }}</el-tag>
          <div class="related-kw" v-if="r.keywords">关键词：{{ r.keywords }}</div>
        </div>
      </div>
      <div v-if="relatedResult.concepts && relatedResult.concepts.length" class="related-group">
        <div class="group-title">相关概念</div>
        <div class="concept-chips">
          <el-tag v-for="c in relatedResult.concepts" :key="c" size="small" class="concept-chip">{{ c }}</el-tag>
        </div>
      </div>
      <div v-if="!relatedResult.regulations.length && !relatedResult.concepts.length" class="related-empty">
        未找到与「{{ keyword }}」相关的法规或概念
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { knowledgeApi } from '@/api'

const loading = ref(false)
const nodes = ref([])
const links = ref([])
const domains = ref([])
const activeDomain = ref('')
const keyword = ref('')
const relatedResult = ref(null)
const selectedId = ref(null)

const width = 1000
const height = 620

const layout = computed(() => computeLayout(nodes.value, links.value, selectedId.value))

async function fetchDomains() {
  try {
    domains.value = await knowledgeApi.kgDomains() || []
  } catch {
    domains.value = []
  }
}

async function fetchGraph() {
  loading.value = true
  try {
    const res = await knowledgeApi.kgGraph(activeDomain.value || undefined)
    nodes.value = res.nodes || []
    links.value = res.links || []
  } catch {
    nodes.value = []
    links.value = []
  } finally {
    loading.value = false
  }
}

function switchDomain(d) {
  activeDomain.value = d
  selectedId.value = null
  fetchGraph()
}

async function searchRelated() {
  if (!keyword.value.trim()) return
  try {
    relatedResult.value = await knowledgeApi.kgRelated(keyword.value.trim())
    // 高亮命中的法规节点
    const hitIds = new Set()
    nodes.value.forEach(n => {
      if (n.type === 'regulation' && n.name.includes(keyword.value.trim())) hitIds.add(n.id)
    })
    selectedId.value = null
    highlightIds.value = hitIds
  } catch {
    relatedResult.value = null
  }
}

function clearSearch() {
  keyword.value = ''
  relatedResult.value = null
  highlightIds.value = new Set()
}

const highlightIds = ref(new Set())

function selectNode(n) {
  selectedId.value = selectedId.value === n.id ? null : n.id
}

function nodeRadius(n) {
  if (n.category === 0) return 20
  if (n.category === 1) return 14
  return 8
}

function isHighlightNode(id) {
  if (highlightIds.value && highlightIds.value.has(id)) return true
  if (!selectedId.value) return false
  if (id === selectedId.value) return true
  // 与选中节点直接相连的节点也高亮
  return links.value.some(l =>
    (l.source === selectedId.value && l.target === id) ||
    (l.target === selectedId.value && l.source === id))
}

function isHighlightLink(l) {
  if (!selectedId.value) return false
  return l.source === selectedId.value || l.target === selectedId.value
}

// --- 拖拽 ---
const dragging = ref(null)
function startDrag(n, e) {
  dragging.value = { id: n.id, startX: e.clientX, startY: e.clientY }
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', endDrag)
}
function onDrag(e) {
  if (!dragging.value) return
  const svg = svgRef.value
  if (!svg) return
  const rect = svg.getBoundingClientRect()
  const scaleX = width / rect.width
  const scaleY = height / rect.height
  const x = (e.clientX - rect.left) * scaleX
  const y = (e.clientY - rect.top) * scaleY
  const n = nodes.value.find(n => n.id === dragging.value.id)
  if (n) { n.x = clamp(x, 10, width - 10); n.y = clamp(y, 10, height - 10) }
}
function endDrag() {
  dragging.value = null
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', endDrag)
}
function clamp(v, min, max) { return Math.min(Math.max(v, min), max) }

// --- 力导向布局 ---
function computeLayout(rawNodes, rawLinks, selected) {
  // 复制并确保有初始坐标
  const ns = rawNodes.map(n => ({ ...n, x: n.x ?? Math.random() * width, y: n.y ?? Math.random() * height }))
  const byId = {}
  ns.forEach(n => { byId[n.id] = n })
  const ls = rawLinks
    .filter(l => byId[l.source] && byId[l.target])
    .map(l => ({ s: l.source, t: l.target, relation: l.relation }))

  // 按 category 分层初始化，保证类型分区清晰
  ns.forEach((n, i) => {
    const cx = width / 2, cy = height / 2
    if (n.category === 0) {
      // 领域：顶部一排
      const gap = width / (ns.filter(x => x.category === 0).length + 1)
      const idx = ns.filter(x => x.category === 0).indexOf(n)
      n.x = gap * (idx + 1); n.y = 60
    } else if (n.category === 1) {
      // 法规：中部
      n.x = width * 0.15 + Math.random() * width * 0.7
      n.y = height * 0.35 + Math.random() * height * 0.3
    } else {
      // 概念：底部
      n.x = width * 0.1 + Math.random() * width * 0.8
      n.y = height * 0.7 + Math.random() * height * 0.2
    }
  })

  // 简单力导向迭代
  const ITER = 300
  for (let it = 0; it < ITER; it++) {
    const fx = {}, fy = {}
    // 斥力
    for (let i = 0; i < ns.length; i++) {
      for (let j = i + 1; j < ns.length; j++) {
        const a = ns[i], b = ns[j]
        let dx = a.x - b.x, dy = a.y - b.y
        let dist = Math.sqrt(dx * dx + dy * dy) || 1
        const rep = 1800 / (dist * dist)
        const f = Math.min(rep, 5)
        fx[a.id] = (fx[a.id] || 0) + (dx / dist) * f
        fy[a.id] = (fy[a.id] || 0) + (dy / dist) * f
        fx[b.id] = (fx[b.id] || 0) - (dx / dist) * f
        fy[b.id] = (fy[b.id] || 0) - (dy / dist) * f
      }
    }
    // 引力（边）
    for (const l of ls) {
      const a = byId[l.s], b = byId[l.t]
      let dx = a.x - b.x, dy = a.y - b.y
      let dist = Math.sqrt(dx * dx + dy * dy) || 1
      const f = dist / 80
      fx[a.id] = (fx[a.id] || 0) - (dx / dist) * f
      fy[a.id] = (fy[a.id] || 0) - (dy / dist) * f
      fx[b.id] = (fx[b.id] || 0) + (dx / dist) * f
      fy[b.id] = (fy[b.id] || 0) + (dy / dist) * f
    }
    // 向心力（拉回画布中心）
    for (const n of ns) {
      fx[n.id] = (fx[n.id] || 0) + (width / 2 - n.x) * 0.002
      fy[n.id] = (fy[n.id] || 0) + (height / 2 - n.y) * 0.002
    }
    // 应用
    for (const n of ns) {
      n.x = clamp(n.x + (fx[n.id] || 0), 20, width - 20)
      n.y = clamp(n.y + (fy[n.id] || 0), 20, height - 20)
    }
  }

  const linkViews = ls.map(l => ({
    x1: byId[l.s].x, y1: byId[l.s].y,
    x2: byId[l.t].x, y2: byId[l.t].y,
    source: l.s, target: l.t, relation: l.relation
  }))
  return { nodes: ns, links: linkViews }
}

onMounted(() => {
  fetchDomains()
  fetchGraph()
})
onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', endDrag)
})
</script>

<style scoped>
.kg-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.domain-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.domain-tag {
  cursor: pointer;
}
.search-box {
  flex-shrink: 0;
}
.legend {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
}
.dot-domain { background: #409eff; }
.dot-reg { background: #67c23a; }
.dot-concept { background: #e6a23c; }
.legend-tip { color: #999; margin-left: auto; }
.graph-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  min-height: 500px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.empty { padding: 60px 0; }
.kg-svg {
  width: 100%;
  height: auto;
  display: block;
  user-select: none;
}
.edge {
  stroke: #d3d8e0;
  stroke-width: 1;
  opacity: 0.7;
}
.edge-hl {
  stroke: #409eff;
  stroke-width: 2;
  opacity: 1;
}
.node { cursor: pointer; }
.node-circle {
  stroke: #fff;
  stroke-width: 2;
  transition: stroke-width 0.15s;
}
.cat0 { fill: #409eff; }
.cat1 { fill: #67c23a; }
.cat2 { fill: #e6a23c; }
.node-hl {
  stroke: #f56c6c;
  stroke-width: 3;
}
.node-label {
  font-size: 11px;
  fill: #555;
  pointer-events: none;
}
.cat0 + .node-label,
.node-label-hl {
  font-weight: 600;
  fill: #303133;
}
.related-panel {
  margin-top: 20px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.related-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.related-group { margin-bottom: 16px; }
.group-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.related-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.related-name { font-weight: 500; }
.related-kw { width: 100%; color: #999; font-size: 13px; }
.concept-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.related-empty { color: #999; padding: 20px 0; }
@media (max-width: 768px) {
  .toolbar { flex-direction: column; }
  .legend-tip { margin-left: 0; }
}
</style>
