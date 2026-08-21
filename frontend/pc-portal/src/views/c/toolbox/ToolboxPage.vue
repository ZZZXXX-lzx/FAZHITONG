<template>
  <div class="toolbox-page">
    <div class="page-header">
      <h2>法律工具箱</h2>
      <p>常用法律费用计算工具，助力您快速估算</p>
    </div>

    <div v-if="!activeTool" class="tool-grid">
      <el-card
        v-for="tool in tools"
        :key="tool.key"
        class="tool-card"
        shadow="hover"
        @click="activeTool = tool.key"
      >
        <el-icon :size="40" class="tool-icon"><component :is="tool.icon" /></el-icon>
        <div class="tool-name">{{ tool.name }}</div>
        <div class="tool-desc">{{ tool.desc }}</div>
      </el-card>
    </div>

    <div v-else class="tool-detail">
      <el-page-header @back="activeTool = ''" :content="currentToolName" style="margin-bottom: 24px" />

      <!-- 诉讼费计算器 -->
      <el-card v-if="activeTool === 'litigation'" class="calc-card">
        <el-form label-width="120px">
          <el-form-item label="诉讼标的额">
            <el-input-number v-model="litigation.amount" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcLitigation">计算</el-button>
            <el-button @click="litigation.result = null">清空</el-button>
          </el-form-item>
        </el-form>
        <el-alert v-if="litigation.result !== null" type="success" :closable="false" style="margin-top: 16px">
          <div class="result-text">诉讼费：{{ litigation.result.toFixed(2) }} 元</div>
        </el-alert>
        <div class="rate-table">
          <h4>收费标准</h4>
          <el-table :data="litigationRates" size="small" border>
            <el-table-column prop="range" label="金额区间" />
            <el-table-column prop="rate" label="费率" width="120" />
          </el-table>
        </div>
      </el-card>

      <!-- 利息计算器 -->
      <el-card v-if="activeTool === 'interest'" class="calc-card">
        <el-form label-width="120px">
          <el-form-item label="本金">
            <el-input-number v-model="interest.principal" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item label="年利率">
            <el-input-number v-model="interest.rate" :min="0" :max="100" :precision="4" :step="0.01" style="width: 240px" />
            <span class="unit">%</span>
          </el-form-item>
          <el-form-item label="期限">
            <el-input-number v-model="interest.years" :min="0" :precision="2" :step="0.5" style="width: 240px" />
            <span class="unit">年</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcInterest">计算</el-button>
            <el-button @click="interest.simpleResult = null; interest.compoundResult = null">清空</el-button>
          </el-form-item>
        </el-form>
        <el-alert v-if="interest.simpleResult !== null" type="success" :closable="false" style="margin-top: 16px">
          <div class="result-text">
            简单利息：{{ interest.simpleResult.toFixed(2) }} 元 | 本息合计：{{ (interest.principal + interest.simpleResult).toFixed(2) }} 元
          </div>
        </el-alert>
        <el-alert v-if="interest.compoundResult !== null" type="warning" :closable="false" style="margin-top: 12px">
          <div class="result-text">
            复利利息：{{ interest.compoundResult.toFixed(2) }} 元 | 本息合计：{{ (interest.principal + interest.compoundResult).toFixed(2) }} 元
          </div>
        </el-alert>
      </el-card>

      <!-- 工伤赔偿计算器 -->
      <el-card v-if="activeTool === 'injury'" class="calc-card">
        <el-form label-width="120px">
          <el-form-item label="月工资">
            <el-input-number v-model="injury.salary" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元/月</span>
          </el-form-item>
          <el-form-item label="伤残等级">
            <el-select v-model="injury.level" placeholder="请选择" style="width: 240px">
              <el-option v-for="n in 10" :key="n" :label="`${n}级`" :value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcInjury">计算</el-button>
            <el-button @click="injury.result = null">清空</el-button>
          </el-form-item>
        </el-form>
        <el-alert v-if="injury.result !== null" type="success" :closable="false" style="margin-top: 16px">
          <div class="result-text">
            一次性伤残补助金：{{ injury.result.toFixed(2) }} 元（{{ injuryMonths[injury.level] }}个月工资）
          </div>
        </el-alert>
        <div class="rate-table">
          <h4>补助标准</h4>
          <el-table :data="injuryTable" size="small" border>
            <el-table-column prop="level" label="伤残等级" width="100" />
            <el-table-column prop="months" label="补助月数" />
          </el-table>
        </div>
      </el-card>

      <!-- 个税计算器 -->
      <el-card v-if="activeTool === 'tax'" class="calc-card">
        <el-form label-width="120px">
          <el-form-item label="月收入">
            <el-input-number v-model="tax.income" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item label="起征点">
            <el-input-number v-model="tax.threshold" :min="0" :step="1000" style="width: 240px" disabled />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item label="专项扣除">
            <el-input-number v-model="tax.deduction" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcTax">计算</el-button>
            <el-button @click="tax.result = null; tax.rate = null">清空</el-button>
          </el-form-item>
        </el-form>
        <el-alert v-if="tax.result !== null" type="success" :closable="false" style="margin-top: 16px">
          <div class="result-text">
            应纳税额：{{ tax.result.toFixed(2) }} 元 | 适用税率：{{ tax.rate }}% | 税后收入：{{ (tax.income - tax.result).toFixed(2) }} 元
          </div>
        </el-alert>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Coin, TrendCharts, Warning, Money } from '@element-plus/icons-vue'

const activeTool = ref('')

const tools = [
  { key: 'litigation', name: '诉讼费计算器', desc: '按财产案件收费标准计算', icon: Coin },
  { key: 'interest', name: '利息计算器', desc: '简单利息与复利计算', icon: TrendCharts },
  { key: 'injury', name: '工伤赔偿计算器', desc: '一次性伤残补助金计算', icon: Warning },
  { key: 'tax', name: '个税计算器', desc: '个人所得税计算', icon: Money },
]

const currentToolName = computed(() => {
  const t = tools.find(t => t.key === activeTool.value)
  return t ? t.name : ''
})

// 诉讼费计算器
const litigation = reactive({
  amount: 100000,
  result: null,
})

const litigationRates = [
  { range: '1万以下', rate: '50元（固定）' },
  { range: '1万 - 10万', rate: '2.5%' },
  { range: '10万 - 20万', rate: '2%' },
  { range: '20万 - 50万', rate: '1.5%' },
  { range: '50万 - 100万', rate: '1%' },
  { range: '100万 - 200万', rate: '0.9%' },
  { range: '200万 - 500万', rate: '0.8%' },
  { range: '500万以上', rate: '0.7%' },
]

function calcLitigation() {
  const amount = litigation.amount
  if (amount <= 0) {
    litigation.result = 0
    return
  }
  let fee = 0
  const segments = [
    { max: 10000, rate: 0 },
    { max: 100000, rate: 0.025 },
    { max: 200000, rate: 0.02 },
    { max: 500000, rate: 0.015 },
    { max: 1000000, rate: 0.01 },
    { max: 2000000, rate: 0.009 },
    { max: 5000000, rate: 0.008 },
    { max: Infinity, rate: 0.007 },
  ]
  let prev = 0
  for (const seg of segments) {
    if (amount > prev) {
      const portion = Math.min(amount, seg.max) - prev
      if (prev === 0 && seg.max === 10000) {
        fee += 50
      } else {
        fee += portion * seg.rate
      }
      prev = seg.max
    } else {
      break
    }
  }
  litigation.result = fee
}

// 利息计算器
const interest = reactive({
  principal: 100000,
  rate: 5,
  years: 1,
  simpleResult: null,
  compoundResult: null,
})

function calcInterest() {
  const p = interest.principal
  const r = interest.rate / 100
  const n = interest.years
  if (p <= 0 || r <= 0 || n <= 0) {
    interest.simpleResult = 0
    interest.compoundResult = 0
    return
  }
  // 简单利息: I = P * r * n
  interest.simpleResult = p * r * n
  // 复利: A = P * (1 + r)^n, I = A - P
  interest.compoundResult = p * Math.pow(1 + r, n) - p
}

// 工伤赔偿计算器
const injury = reactive({
  salary: 8000,
  level: 10,
  result: null,
})

const injuryMonths = {
  1: 27, 2: 25, 3: 23, 4: 21, 5: 18,
  6: 16, 7: 13, 8: 11, 9: 9, 10: 7,
}

const injuryTable = [
  { level: '1级', months: '27个月' },
  { level: '2级', months: '25个月' },
  { level: '3级', months: '23个月' },
  { level: '4级', months: '21个月' },
  { level: '5级', months: '18个月' },
  { level: '6级', months: '16个月' },
  { level: '7级', months: '13个月' },
  { level: '8级', months: '11个月' },
  { level: '9级', months: '9个月' },
  { level: '10级', months: '7个月' },
]

function calcInjury() {
  if (!injury.level || injury.salary <= 0) {
    injury.result = 0
    return
  }
  const months = injuryMonths[injury.level] || 0
  injury.result = injury.salary * months
}

// 个税计算器
const tax = reactive({
  income: 20000,
  threshold: 5000,
  deduction: 0,
  result: null,
  rate: null,
})

function calcTax() {
  const taxable = tax.income - tax.threshold - tax.deduction
  if (taxable <= 0) {
    tax.result = 0
    tax.rate = 0
    return
  }
  const brackets = [
    { max: 3000, rate: 0.03, deduction: 0 },
    { max: 12000, rate: 0.1, deduction: 210 },
    { max: 25000, rate: 0.2, deduction: 1410 },
    { max: 35000, rate: 0.25, deduction: 2660 },
    { max: 55000, rate: 0.3, deduction: 4410 },
    { max: 80000, rate: 0.35, deduction: 7160 },
    { max: Infinity, rate: 0.45, deduction: 15160 },
  ]
  let prev = 0
  for (const b of brackets) {
    if (taxable > prev) {
      if (taxable <= b.max) {
        tax.result = taxable * b.rate - b.deduction
        tax.rate = (b.rate * 100).toFixed(0)
        return
      }
      prev = b.max
    }
  }
  tax.result = 0
  tax.rate = 0
}
</script>

<style scoped>
.toolbox-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}
.page-header p {
  color: #666;
}
.tool-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.tool-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}
.tool-card:hover {
  transform: translateY(-4px);
}
.tool-icon {
  color: #409eff;
  margin-bottom: 12px;
}
.tool-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}
.tool-desc {
  font-size: 13px;
  color: #999;
}
.calc-card {
  max-width: 640px;
}
.unit {
  margin-left: 8px;
  color: #999;
  font-size: 13px;
}
.result-text {
  font-size: 16px;
  font-weight: 600;
}
.rate-table {
  margin-top: 24px;
}
.rate-table h4 {
  margin-bottom: 12px;
  color: #333;
}
@media (max-width: 768px) {
  .tool-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 480px) {
  .tool-grid {
    grid-template-columns: 1fr;
  }
}
</style>
