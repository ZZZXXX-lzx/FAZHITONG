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
        <el-form label-width="140px">
          <el-form-item label="本人工资（月）">
            <el-input-number v-model="injury.salary" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元/月</span>
          </el-form-item>
          <el-form-item label="伤残等级">
            <el-select v-model="injury.level" placeholder="请选择" style="width: 240px">
              <el-option v-for="n in 10" :key="n" :label="`${n}级`" :value="n" />
            </el-select>
          </el-form-item>
          <el-form-item label="停工留薪期">
            <el-input-number v-model="injury.leaveMonths" :min="0" :max="24" :step="1" style="width: 240px" />
            <span class="unit">月</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcInjury">计算</el-button>
            <el-button @click="clearInjury">清空</el-button>
          </el-form-item>
        </el-form>
        <p class="calc-note">停工留薪期一般不超过12个月，伤情严重或特殊可延长，最长不超过24个月；伤残津贴1-4级按月支付至退休，5-6级由用人单位安排工作、难以安排的按月发放。</p>

        <template v-if="injury.computed">
          <el-alert type="success" :closable="false" style="margin-top: 16px">
            <div class="result-text">
              一次性合计可获：{{ injury.totalLumpSum.toFixed(2) }} 元（不含按月发放的伤残津贴）
            </div>
          </el-alert>

          <div class="calc-steps">
            <h4>计算过程</h4>
            <div class="step">
              <span class="step-no">1</span>
              <div class="step-body">
                <strong>一次性伤残补助金</strong>
                <p>一次性伤残补助金 = 本人工资 × {{ injuryMonths[injury.level] }} 个月（{{ injury.level }}级伤残标准）</p>
                <p class="formula">{{ injury.salary.toFixed(2) }} × {{ injuryMonths[injury.level] }} = <b>{{ injury.disabilityBenefit.toFixed(2) }}</b> 元</p>
              </div>
            </div>
            <div class="step">
              <span class="step-no">2</span>
              <div class="step-body">
                <strong>停工留薪期工资</strong>
                <p>停工留薪期工资 = 本人工资 × 停工留薪期月数（治疗期间原工资福利待遇不变）</p>
                <p class="formula">{{ injury.salary.toFixed(2) }} × {{ injury.leaveMonths }} = <b>{{ injury.leavePay.toFixed(2) }}</b> 元</p>
              </div>
            </div>
            <div class="step">
              <span class="step-no">3</span>
              <div class="step-body">
                <strong>一次性合计</strong>
                <p>一次性合计 = 一次性伤残补助金 + 停工留薪期工资</p>
                <p class="formula">{{ injury.disabilityBenefit.toFixed(2) }} + {{ injury.leavePay.toFixed(2) }} = <b>{{ injury.totalLumpSum.toFixed(2) }}</b> 元</p>
              </div>
            </div>
            <div class="step" v-if="injury.allowanceMonthly > 0">
              <span class="step-no">4</span>
              <div class="step-body">
                <strong>伤残津贴（按月发放）</strong>
                <p>伤残津贴 = 本人工资 × {{ injury.allowanceRate }}%（{{ injury.level }}级伤残标准，按月支付）</p>
                <p class="formula">{{ injury.salary.toFixed(2) }} × {{ injury.allowanceRate }}% = <b>{{ injury.allowanceMonthly.toFixed(2) }}</b> 元/月</p>
              </div>
            </div>
          </div>

          <p class="calc-note" style="margin-top: 8px">注：5-10级伤残职工解除或终止劳动合同时，还可依法领取一次性工伤医疗补助金和一次性伤残就业补助金，具体标准由各省、自治区、直辖市人民政府规定，本计算器未包含该部分。</p>
        </template>

        <div class="rate-table">
          <h4>补助标准（一次性补助金月数 / 伤残津贴比例）</h4>
          <el-table :data="injuryTable" size="small" border>
            <el-table-column prop="level" label="伤残等级" width="100" align="center" />
            <el-table-column prop="months" label="补助金月数" align="center" />
            <el-table-column prop="allowance" label="伤残津贴" align="center" />
          </el-table>
        </div>
      </el-card>

      <!-- 个税计算器 -->
      <el-card v-if="activeTool === 'tax'" class="calc-card">
        <el-form label-width="140px">
          <el-form-item label="月工资收入">
            <el-input-number v-model="tax.income" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item label="五险一金（专项扣除）">
            <el-input-number v-model="tax.social" :min="0" :precision="2" style="width: 240px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item v-for="item in deductionItems" :key="item.key" :label="item.name">
            <el-input-number v-model="tax.deduct[item.key]" :min="0" :precision="2" style="width: 170px" />
            <span class="deduct-hint">{{ item.standard }}</span>
          </el-form-item>
          <el-form-item label="起征点">
            <el-input-number v-model="tax.threshold" :min="0" :step="1000" style="width: 240px" disabled />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calcTax">计算</el-button>
            <el-button @click="clearTax">清空</el-button>
          </el-form-item>
        </el-form>

        <template v-if="tax.result !== null">
          <el-alert type="success" :closable="false" style="margin-top: 16px">
            <div class="result-text">
              应纳税额：{{ tax.result.toFixed(2) }} 元 ｜ 税后收入：{{ tax.afterTax.toFixed(2) }} 元
            </div>
          </el-alert>

          <div class="tax-steps">
            <h4>计算过程</h4>
            <div class="step">
              <span class="step-no">1</span>
              <div class="step-body">
                <strong>计算应纳税所得额</strong>
                <p>应纳税所得额 = 月工资收入 - 起征点(5000) - 五险一金 - 专项附加扣除</p>
                <div class="deduct-list">
                  <div class="deduct-row" v-for="item in deductionItems" :key="item.key">
                    <span>{{ item.name }}</span>
                    <span>{{ (tax.deduct[item.key] || 0).toFixed(2) }} 元</span>
                  </div>
                  <div class="deduct-row total">
                    <span>专项附加扣除合计</span>
                    <span>{{ totalDeduction.toFixed(2) }} 元</span>
                  </div>
                </div>
                <p class="formula">{{ tax.income.toFixed(2) }} - {{ tax.threshold }} - {{ tax.social.toFixed(2) }} - {{ totalDeduction.toFixed(2) }} = <b>{{ tax.taxable.toFixed(2) }}</b> 元</p>
              </div>
            </div>
            <template v-if="tax.taxable > 0">
              <div class="step">
                <span class="step-no">2</span>
                <div class="step-body">
                  <strong>确定适用税率与速算扣除数</strong>
                  <p>应纳税所得额 {{ tax.taxable.toFixed(2) }} 元，落入「{{ tax.bracketLabel }}」档：</p>
                  <p class="formula">适用税率 <b>{{ tax.rate }}%</b>，速算扣除数 <b>{{ tax.quickDeduction }}</b> 元</p>
                </div>
              </div>
              <div class="step">
                <span class="step-no">3</span>
                <div class="step-body">
                  <strong>计算应纳税额</strong>
                  <p>应纳税额 = 应纳税所得额 × 税率 - 速算扣除数（速算扣除数用于简化超额累进税率计算）</p>
                  <p class="formula">{{ tax.taxable.toFixed(2) }} × {{ tax.rate }}% - {{ tax.quickDeduction }} = <b>{{ tax.result.toFixed(2) }}</b> 元</p>
                </div>
              </div>
              <div class="step">
                <span class="step-no">4</span>
                <div class="step-body">
                  <strong>计算税后收入</strong>
                  <p>税后收入 = 月工资收入 - 五险一金 - 应纳税额</p>
                  <p class="formula">{{ tax.income.toFixed(2) }} - {{ tax.social.toFixed(2) }} - {{ tax.result.toFixed(2) }} = <b>{{ tax.afterTax.toFixed(2) }}</b> 元</p>
                </div>
              </div>
            </template>
            <el-alert v-else type="info" :closable="false" style="margin-top: 4px">
              应纳税所得额 ≤ 0，本月无需缴纳个人所得税，税后收入 = 月工资收入 - 五险一金 = {{ tax.afterTax.toFixed(2) }} 元。
            </el-alert>
          </div>
        </template>

        <div class="rate-table">
          <h4>综合所得月度税率表（居民个人工资薪金）</h4>
          <el-table :data="taxBrackets" size="small" border>
            <el-table-column prop="range" label="应纳税所得额（月）" />
            <el-table-column prop="rate" label="税率" width="80" align="center" />
            <el-table-column prop="deduction" label="速算扣除数" width="110" align="center" />
          </el-table>
        </div>
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
  leaveMonths: 12,
  computed: false,
  disabilityBenefit: 0,
  leavePay: 0,
  totalLumpSum: 0,
  allowanceMonthly: 0,
  allowanceRate: 0,
})

const injuryMonths = {
  1: 27, 2: 25, 3: 23, 4: 21, 5: 18,
  6: 16, 7: 13, 8: 11, 9: 9, 10: 7,
}

const injuryAllowanceRate = {
  1: 90, 2: 85, 3: 80, 4: 75, 5: 70, 6: 60,
}

const injuryTable = [
  { level: '1级', months: '27个月', allowance: '90%' },
  { level: '2级', months: '25个月', allowance: '85%' },
  { level: '3级', months: '23个月', allowance: '80%' },
  { level: '4级', months: '21个月', allowance: '75%' },
  { level: '5级', months: '18个月', allowance: '70%' },
  { level: '6级', months: '16个月', allowance: '60%' },
  { level: '7级', months: '13个月', allowance: '—' },
  { level: '8级', months: '11个月', allowance: '—' },
  { level: '9级', months: '9个月', allowance: '—' },
  { level: '10级', months: '7个月', allowance: '—' },
]

function clearInjury() {
  injury.computed = false
  injury.disabilityBenefit = 0
  injury.leavePay = 0
  injury.totalLumpSum = 0
  injury.allowanceMonthly = 0
  injury.allowanceRate = 0
}

function calcInjury() {
  injury.computed = true
  const salary = injury.salary || 0
  const level = injury.level
  const leaveMonths = injury.leaveMonths || 0

  if (!level || salary <= 0) {
    injury.disabilityBenefit = 0
    injury.leavePay = 0
    injury.totalLumpSum = 0
    injury.allowanceMonthly = 0
    injury.allowanceRate = 0
    return
  }

  const months = injuryMonths[level] || 0
  injury.disabilityBenefit = salary * months

  const rate = injuryAllowanceRate[level] || 0
  injury.allowanceRate = rate
  injury.allowanceMonthly = rate > 0 ? salary * rate / 100 : 0

  injury.leavePay = salary * leaveMonths
  injury.totalLumpSum = injury.disabilityBenefit + injury.leavePay
}

// 个税计算器
const tax = reactive({
  income: 20000,
  social: 0,
  threshold: 5000,
  result: null,
  rate: 0,
  taxable: 0,
  quickDeduction: 0,
  bracketLabel: '',
  afterTax: 0,
  deduct: {
    childrenEducation: 0,
    continuingEducation: 0,
    medicalExpense: 0,
    housingLoanInterest: 0,
    housingRent: 0,
    elderlySupport: 0,
    infantCare: 0,
  },
})

const deductionItems = [
  { key: 'childrenEducation', name: '子女教育', standard: '2000元/月/每个子女' },
  { key: 'continuingEducation', name: '继续教育', standard: '学历400元/月，职业资格3600元/年' },
  { key: 'medicalExpense', name: '大病医疗', standard: '年度限额80000元内据实扣除' },
  { key: 'housingLoanInterest', name: '住房贷款利息', standard: '1000元/月（首套）' },
  { key: 'housingRent', name: '住房租金', standard: '1500/1100/800元/月（按城市）' },
  { key: 'elderlySupport', name: '赡养老人', standard: '独生3000元/月，非独生最高1500元/月' },
  { key: 'infantCare', name: '3岁以下婴幼儿照护', standard: '2000元/月/每个婴幼儿' },
]

const totalDeduction = computed(() => {
  const d = tax.deduct
  return (d.childrenEducation || 0) + (d.continuingEducation || 0) + (d.medicalExpense || 0) +
    (d.housingLoanInterest || 0) + (d.housingRent || 0) + (d.elderlySupport || 0) + (d.infantCare || 0)
})

const taxBrackets = [
  { range: '不超过 3,000 元', rate: '3%', deduction: '0' },
  { range: '3,000 ~ 12,000 元', rate: '10%', deduction: '210' },
  { range: '12,000 ~ 25,000 元', rate: '20%', deduction: '1,410' },
  { range: '25,000 ~ 35,000 元', rate: '25%', deduction: '2,660' },
  { range: '35,000 ~ 55,000 元', rate: '30%', deduction: '4,410' },
  { range: '55,000 ~ 80,000 元', rate: '35%', deduction: '7,160' },
  { range: '超过 80,000 元', rate: '45%', deduction: '15,160' },
]

function clearTax() {
  tax.result = null
  tax.rate = 0
  tax.taxable = 0
  tax.quickDeduction = 0
  tax.bracketLabel = ''
  tax.afterTax = 0
}

function calcTax() {
  const income = tax.income || 0
  const social = tax.social || 0
  const deduction = totalDeduction.value || 0
  const threshold = tax.threshold || 5000
  const taxable = income - threshold - social - deduction
  tax.taxable = taxable

  if (taxable <= 0) {
    tax.result = 0
    tax.rate = 0
    tax.quickDeduction = 0
    tax.bracketLabel = '不超过 3,000 元'
    tax.afterTax = income - social
    return
  }

  const brackets = [
    { max: 3000, rate: 0.03, deduction: 0, label: '不超过 3,000 元' },
    { max: 12000, rate: 0.1, deduction: 210, label: '3,000 ~ 12,000 元' },
    { max: 25000, rate: 0.2, deduction: 1410, label: '12,000 ~ 25,000 元' },
    { max: 35000, rate: 0.25, deduction: 2660, label: '25,000 ~ 35,000 元' },
    { max: 55000, rate: 0.3, deduction: 4410, label: '35,000 ~ 55,000 元' },
    { max: 80000, rate: 0.35, deduction: 7160, label: '55,000 ~ 80,000 元' },
    { max: Infinity, rate: 0.45, deduction: 15160, label: '超过 80,000 元' },
  ]

  const bracket = brackets.find(b => taxable <= b.max)
  tax.rate = Math.round(bracket.rate * 100)
  tax.quickDeduction = bracket.deduction
  tax.bracketLabel = bracket.label
  tax.result = taxable * bracket.rate - bracket.deduction
  tax.afterTax = income - social - tax.result
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
.tax-note {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
  margin: -4px 0 12px;
}
.tax-steps {
  margin-top: 20px;
}
.tax-steps h4 {
  margin-bottom: 14px;
  color: #333;
}
.step {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.step-no {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}
.step-body {
  flex: 1;
}
.step-body strong {
  display: block;
  margin-bottom: 4px;
  color: #1a1a2e;
}
.step-body p {
  margin: 2px 0;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
.step-body .formula {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 8px 10px;
  font-family: Consolas, Menlo, monospace;
  color: #333;
}
.step-body .formula b {
  color: #e74c3c;
}
.deduct-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}
.deduct-list {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 8px 12px;
  margin: 6px 0;
}
.deduct-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  line-height: 1.9;
}
.deduct-row.total {
  border-top: 1px dashed #dcdfe6;
  margin-top: 4px;
  padding-top: 4px;
  color: #333;
  font-weight: 600;
}
.calc-steps {
  margin-top: 20px;
}
.calc-steps h4 {
  margin-bottom: 14px;
  color: #333;
}
.calc-note {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
  margin: -4px 0 12px;
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
