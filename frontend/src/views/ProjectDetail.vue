<template>
  <div class="project-detail" v-loading="loading">
    <div class="top-bar">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/' }">仪表盘</el-breadcrumb-item>
        <el-breadcrumb-item>{{ project?.name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="top-actions">
        <el-button @click="runAIHealthCheck" :loading="aiChecking" type="warning" plain>
          <el-icon><Cpu /></el-icon> AI 健康诊断
        </el-button>
        <el-dropdown @command="generateAIReport">
          <el-button type="primary" plain>
            <el-icon><Document /></el-icon> AI 报告
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="weekly">周报</el-dropdown-item>
              <el-dropdown-item command="monthly">月报</el-dropdown-item>
              <el-dropdown-item command="review">复盘</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openAddTask">
          <el-icon><Plus /></el-icon> 新增任务
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row" v-if="stats">
      <div class="stat-card">
        <div class="stat-num">{{ stats.completionPct }}%</div>
        <div class="stat-name">完成率</div>
        <el-progress :percentage="stats.completionPct" :color="pColor(stats.completionPct)" />
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.doneTasks }}/{{ stats.totalTasks }}</div>
        <div class="stat-name">已完成/总任务</div>
      </div>
      <div class="stat-card warn" v-if="stats.overdueTasks > 0">
        <div class="stat-num red">{{ stats.overdueTasks }}</div>
        <div class="stat-name">逾期任务</div>
      </div>
      <div class="stat-card warn" v-if="stats.blockedTasks > 0">
        <div class="stat-num orange">{{ stats.blockedTasks }}</div>
        <div class="stat-name">阻塞任务</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.estimatedTotalHours ?? 0 }}h</div>
        <div class="stat-name">预估工时</div>
      </div>
    </div>

    <!-- AI 诊断结果 -->
    <el-alert v-if="aiResult" :title="aiTitle(aiResult.healthStatus)" :type="aiType(aiResult.healthStatus)"
      :description="aiResult.content" closable show-icon style="margin-bottom:20px;white-space:pre-wrap" />

    <!-- 报告 -->
    <div v-if="reports.length" class="reports-section">
      <h4>AI 生成报告</h4>
      <div class="report-list">
        <div v-for="r in reports" :key="r.id" class="report-item" @click="viewReport(r)">
          <el-tag :type="r.type==='weekly'?'':r.type==='monthly'?'success':'warning'" size="small">
            {{ r.type==='weekly'?'周报':r.type==='monthly'?'月报':'复盘' }}
          </el-tag>
          <span class="report-title">{{ r.title }}</span>
          <span class="report-date">{{ r.createTime?.substring(0,10) }}</span>
          <el-button text type="danger" size="small" @click.stop="delReport(r.id)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- Tab 面板 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ====== 任务看板 ====== -->
      <el-tab-pane label="任务看板" name="tasks">
        <el-table :data="tasks" stripe>
          <el-table-column prop="title" label="任务" min-width="220" />
          <el-table-column prop="assigneeName" label="负责人" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="sTag(row.status)" size="small">{{ sLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="70">
            <template #default="{row}">{{ row.priority==='high'?'高':row.priority==='low'?'低':'中' }}</template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="110">
            <template #default="{row}"><el-progress :percentage="row.progress" :stroke-width="6" /></template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止" width="105" />
          <el-table-column label="操作" width="130">
            <template #default="{row}">
              <el-button text type="primary" size="small" @click="editTask(row)">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="delTask(row.id)">
                <template #reference><el-button text type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ====== 燃尽图 ====== -->
      <el-tab-pane label="燃尽图" name="burn">
        <div v-if="burnData && burnData.labels && burnData.labels.length > 0" class="burn-chart">
          <div class="chart-legend">
            <span class="legend-line ideal">━━ 理想线</span>
            <span class="legend-line actual">━━ 实际线</span>
          </div>
          <div class="chart-svg">
            <svg :width="burnW" :height="280" style="background:#fff;border-radius:8px">
              <!-- 网格线 -->
              <line v-for="i in 5" :key="'g'+i" :x1="40" :y1="20+i*50" :x2="burnW-20" :y2="20+i*50"
                stroke="#ebeef5" stroke-width="1" />
              <!-- Y 轴标签 -->
              <text v-for="i in 5" :key="'y'+i" x="32" :y="24+i*50" text-anchor="end" font-size="10" fill="#909399">
                {{ Math.round(burnMax*(5-i)/5) }}
              </text>
              <!-- 理想线 -->
              <polyline :points="burnIdeal" fill="none" stroke="#909399" stroke-width="2" stroke-dasharray="6,3" />
              <!-- 实际线 -->
              <polyline :points="burnActual" fill="none" stroke="#409eff" stroke-width="2.5" />
              <!-- 今日线 -->
              <line :x1="burnTodayX" :y1="20" :x2="burnTodayX" :y2="260" stroke="#e6a23c" stroke-dasharray="4,4" stroke-width="1" />
              <text :x="burnTodayX" y="275" text-anchor="middle" font-size="11" fill="#e6a23c">今日</text>
            </svg>
          </div>
        </div>
        <el-empty v-else description="暂无燃尽图数据" />
      </el-tab-pane>

      <!-- ====== 风险分析 ====== -->
      <el-tab-pane label="风险分析" name="risk">
        <!-- 延期预测 -->
        <h4 style="margin-bottom:12px">⏰ 延期预测</h4>
        <div v-if="delayPreds.length" class="risk-list">
          <div v-for="d in delayPreds" :key="d.taskId" class="risk-item" :class="'risk-'+d.riskLevel">
            <div class="risk-header">
              <el-tag :type="d.riskLevel==='high'?'danger':d.riskLevel==='medium'?'warning':'success'" size="small">
                {{ d.riskLevel==='high'?'高风险':d.riskLevel==='medium'?'中风险':'低风险' }}
              </el-tag>
              <span class="risk-title">{{ d.taskTitle }}</span>
              <span class="risk-meta">{{ d.assigneeName }} · 进度{{ d.currentProgress }}% · 剩余{{ d.daysRemaining }}天</span>
            </div>
            <div class="risk-body">
              <span>预测最终进度：<b>{{ d.predictedFinalProgress }}%</b></span>
              <span v-if="d.dailyVelocity>0"> · 日均推进：{{ d.dailyVelocity }}%</span>
            </div>
            <p class="risk-suggestion">{{ d.suggestion }}</p>
          </div>
        </div>
        <el-empty v-else description="暂无延期风险" :image-size="60" />

        <!-- 阻塞影响 -->
        <h4 style="margin:24px 0 12px">🔗 阻塞影响分析</h4>
        <div v-if="blockedImpacts.length" class="risk-list">
          <div v-for="b in blockedImpacts" :key="b.blockedTaskId" class="risk-item risk-high">
            <div class="risk-header">
              <el-tag type="danger" size="small">阻塞源</el-tag>
              <span class="risk-title">{{ b.blockedTaskTitle }}</span>
            </div>
            <p class="risk-suggestion">{{ b.riskSummary }}</p>
            <div v-if="b.affectedTasks && b.affectedTasks.length" class="affected-list">
              <div v-for="a in b.affectedTasks" :key="a.taskId" class="affected-item">
                → {{ a.title }}（{{ a.assigneeName }}）
                <el-tag :type="sTag(a.status)" size="small" style="margin-left:6px">{{ sLabel(a.status) }}</el-tag>
                <span class="affected-dist">依赖深度: {{ a.distance }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="无阻塞任务" :image-size="60" />

        <!-- 工时偏差 -->
        <h4 style="margin:24px 0 12px">📊 工时偏差预警</h4>
        <div v-if="hourDevs.length">
          <el-table :data="hourDevs" stripe size="small">
            <el-table-column prop="taskTitle" label="任务" min-width="180" />
            <el-table-column prop="assigneeName" label="负责人" width="80" />
            <el-table-column label="预估" width="70">
              <template #default="{row}">{{ row.estimatedHours }}h</template>
            </el-table-column>
            <el-table-column label="实际" width="70">
              <template #default="{row}">{{ row.actualHours }}h</template>
            </el-table-column>
            <el-table-column label="偏差" width="100">
              <template #default="{row}">
                <span :style="{color:row.deviationPct>0?'#f56c6c':'#67c23a'}">
                  {{ row.deviationPct>0?'+':'' }}{{ row.deviationPct }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="等级" width="80">
              <template #default="{row}">
                <el-tag :type="row.level==='high'?'danger':row.level==='medium'?'warning':'success'" size="small">
                  {{ row.level==='high'?'严重':row.level==='medium'?'偏高':'正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无可评估的任务" :image-size="60" />
      </el-tab-pane>

      <!-- ====== 里程碑 ====== -->
      <el-tab-pane label="里程碑" name="milestones">
        <div style="margin-bottom:12px">
          <el-button size="small" type="primary" @click="openAddMs"><el-icon><Plus /></el-icon> 新增里程碑</el-button>
        </div>
        <el-timeline v-if="milestones.length">
          <el-timeline-item
            v-for="m in milestones" :key="m.id"
            :timestamp="m.targetDate"
            :type="m.status==='achieved'?'success':m.status==='missed'?'danger':'primary'"
            :hollow="m.status==='pending'"
          >
            <div class="ms-item">
              <span class="ms-name">{{ m.name }}</span>
              <el-tag :type="m.status==='achieved'?'success':m.status==='missed'?'danger':'info'" size="small">
                {{ m.status==='achieved'?'已完成':m.status==='missed'?'已逾期':'待完成' }}
              </el-tag>
              <span v-if="m.description" class="ms-desc">{{ m.description }}</span>
              <div class="ms-actions">
                <el-button text type="primary" size="small" @click="editMs(m)">编辑</el-button>
                <el-popconfirm title="确认删除？" @confirm="delMs(m.id)">
                  <template #reference><el-button text type="danger" size="small">删除</el-button></template>
                </el-popconfirm>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无里程碑" />
      </el-tab-pane>

      <!-- ====== 干系人 ====== -->
      <el-tab-pane label="干系人" name="stakeholders">
        <div style="margin-bottom:12px">
          <el-button size="small" type="primary" @click="openAddSh"><el-icon><Plus /></el-icon> 新增干系人</el-button>
        </div>
        <el-table :data="stakeholders" stripe v-if="stakeholders.length">
          <el-table-column prop="name" label="姓名" width="90" />
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="department" label="部门" width="140" />
          <el-table-column label="影响力" width="90">
            <template #default="{row}">
              <el-tag :type="row.influence==='high'?'danger':row.influence==='normal'?'warning':'info'" size="small">
                {{ row.influence==='high'?'高':row.influence==='normal'?'中':'低' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contact" label="联系方式" width="150" />
          <el-table-column prop="expectations" label="期望/关注点" min-width="160" />
          <el-table-column label="操作" width="120">
            <template #default="{row}">
              <el-button text type="primary" size="small" @click="editSh(row)">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="delSh(row.id)">
                <template #reference><el-button text type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无干系人" />
      </el-tab-pane>

      <!-- ====== 风险管理 ====== -->
      <el-tab-pane label="风险管理" name="risks">
        <div style="margin-bottom:12px">
          <el-button size="small" type="primary" @click="openAddRisk"><el-icon><Plus /></el-icon> 登记风险</el-button>
        </div>
        <el-table :data="risks" stripe v-if="risks.length">
          <el-table-column label="级别" width="80">
            <template #default="{row}">
              <el-tag :type="row.level==='high'?'danger':row.level==='medium'?'warning':'success'" size="small">
                {{ row.level==='high'?'高':row.level==='medium'?'中':'低' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="风险描述" min-width="200" />
          <el-table-column prop="category" label="类别" width="90" />
          <el-table-column label="概率/影响" width="100">
            <template #default="{row}">{{ probLabel(row.probability) }}/{{ impLabel(row.impact) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="row.status==='closed'?'success':row.status==='mitigating'?'warning':'danger'" size="small">
                {{ riskStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ownerName" label="责任人" width="80" />
          <el-table-column prop="mitigation" label="应对措施" min-width="160" />
          <el-table-column prop="identifiedDate" label="识别日" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="{row}">
              <el-button text type="primary" size="small" @click="editRisk(row)">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="delRisk(row.id)">
                <template #reference><el-button text type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无已登记风险" :image-size="60" />
      </el-tab-pane>

      <!-- ====== 成员负载 ====== -->
      <el-tab-pane label="负载" name="load">
        <div class="member-load">
          <div v-for="m in memberLoad" :key="m.assigneeId" class="load-bar-wrap">
            <div class="load-name">{{ m.assigneeName }}</div>
            <div class="load-bar"><div class="load-fill" :style="{width:loadPct(m)+'%'}"></div></div>
            <div class="load-info">{{ m.doneCount }}/{{ m.taskCount }} 已完成 · {{ m.estimatedHours }}h</div>
          </div>
        </div>
        <el-empty v-if="!memberLoad.length" description="暂无成员数据" :image-size="60" />
      </el-tab-pane>
    </el-tabs>

    <!-- 任务对话框 -->
    <el-dialog v-model="showTaskDlg" :title="editingTask?'编辑任务':'新增任务'" width="520px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="taskF.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="taskF.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskF.assigneeId" clearable><el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" /></el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="taskF.status">
            <el-option label="待办" value="todo" /><el-option label="进行中" value="in_progress" /><el-option label="已完成" value="done" /><el-option label="阻塞" value="blocked" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskF.priority"><el-option label="高" value="high" /><el-option label="中" value="normal" /><el-option label="低" value="low" /></el-select>
        </el-form-item>
        <el-form-item label="预估工时"><el-input-number v-model="taskF.estimatedHours" :min="0" /></el-form-item>
        <el-form-item label="进度"><el-slider v-model="taskF.progress" :max="100" show-input /></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="taskF.dueDate" type="date" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="taskF.tags" placeholder="逗号分隔" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showTaskDlg=false">取消</el-button><el-button type="primary" @click="saveTask" :loading="savingTask">保存</el-button></template>
    </el-dialog>

    <!-- 里程碑对话框 -->
    <el-dialog v-model="showMsDlg" :title="editingMs?'编辑里程碑':'新增里程碑'" width="460px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="msF.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="msF.description" /></el-form-item>
        <el-form-item label="目标日期"><el-date-picker v-model="msF.targetDate" type="date" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="msF.status"><el-option label="待完成" value="pending" /><el-option label="已完成" value="achieved" /><el-option label="已逾期" value="missed" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showMsDlg=false">取消</el-button><el-button type="primary" @click="saveMs" :loading="savingMs">保存</el-button></template>
    </el-dialog>

    <!-- 干系人对话框 -->
    <el-dialog v-model="showShDlg" :title="editingSh?'编辑干系人':'新增干系人'" width="460px">
      <el-form label-width="80px">
        <el-form-item label="姓名"><el-input v-model="shF.name" /></el-form-item>
        <el-form-item label="角色"><el-input v-model="shF.role" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="shF.department" /></el-form-item>
        <el-form-item label="影响力">
          <el-select v-model="shF.influence"><el-option label="高" value="high" /><el-option label="中" value="normal" /><el-option label="低" value="low" /></el-select>
        </el-form-item>
        <el-form-item label="联系方式"><el-input v-model="shF.contact" /></el-form-item>
        <el-form-item label="期望关注点"><el-input v-model="shF.expectations" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showShDlg=false">取消</el-button><el-button type="primary" @click="saveSh" :loading="savingSh">保存</el-button></template>
    </el-dialog>

    <!-- 风险对话框 -->
    <el-dialog v-model="showRiskDlg" :title="editingRisk?'编辑风险':'登记风险'" width="520px">
      <el-form label-width="80px">
        <el-form-item label="风险描述"><el-input v-model="riskF.title" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="riskF.category">
            <el-option label="技术风险" value="技术风险" /><el-option label="资源风险" value="资源风险" />
            <el-option label="进度风险" value="进度风险" /><el-option label="外部风险" value="外部风险" />
            <el-option label="合规风险" value="合规风险" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生概率">
          <el-select v-model="riskF.probability"><el-option label="高" value="high" /><el-option label="中" value="medium" /><el-option label="低" value="low" /></el-select>
        </el-form-item>
        <el-form-item label="影响程度">
          <el-select v-model="riskF.impact"><el-option label="高" value="high" /><el-option label="中" value="medium" /><el-option label="低" value="low" /></el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="riskF.status"><el-option label="待处理" value="open" /><el-option label="缓解中" value="mitigating" /><el-option label="已关闭" value="closed" /></el-select>
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="riskF.ownerId" clearable><el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" /></el-select>
        </el-form-item>
        <el-form-item label="应对措施"><el-input v-model="riskF.mitigation" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="识别日期"><el-date-picker v-model="riskF.identifiedDate" type="date" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showRiskDlg=false">取消</el-button><el-button type="primary" @click="saveRisk" :loading="savingRisk">保存</el-button></template>
    </el-dialog>

    <!-- 报告预览 -->
    <el-dialog v-model="showReport" title="报告详情" width="680px">
      <div style="white-space:pre-wrap;line-height:1.8">{{ reportContent }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Cpu, Document, ArrowDown } from '@element-plus/icons-vue'
import {
  getProject, getProjectStats, getTasks, createTask, updateTask, deleteTask,
  getMembers, getMemberLoad, runHealthCheck, generateReport, getReports, getReportDetail, deleteReport,
  getBurnChart, getDelayPrediction, getBlockedImpact, getHourDeviations,
  getMilestones, createMilestone, updateMilestone, deleteMilestone,
  getStakeholders, createStakeholder, updateStakeholder, deleteStakeholder,
  getRisks, createRisk, updateRisk, deleteRisk
} from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const projectId = Number(route.params.id)
const activeTab = ref('tasks')

const project = ref(null); const stats = ref(null); const tasks = ref([])
const members = ref([]); const memberLoad = ref([]); const reports = ref([])
const loading = ref(false); const aiChecking = ref(false); const aiResult = ref(null)

// 分析数据
const burnData = ref(null)
const delayPreds = ref([]); const blockedImpacts = ref([]); const hourDevs = ref([])
const milestones = ref([]); const stakeholders = ref([]); const risks = ref([])

// 燃尽图计算
const burnW = ref(800)
const burnMax = computed(() => burnData.value && burnData.value.idealLine ? burnData.value.idealLine[0] || 10 : 10)
const burnStep = computed(() => burnData.value && burnData.value.labels ? (burnW.value - 60) / (burnData.value.labels.length - 1 || 1) : 0)
const burnIdeal = computed(() => burnData.value && burnData.value.idealLine ? burnData.value.idealLine.map((v, i) => `${40 + i * burnStep.value},${20 + 240 * (1 - v / burnMax.value)}`).join(' ') : '')
const burnActual = computed(() => burnData.value && burnData.value.actualLine ? burnData.value.actualLine.map((v, i) => `${40 + i * burnStep.value},${20 + 240 * (1 - v / burnMax.value)}`).join(' ') : '')
const burnTodayX = computed(() => { if (!burnData.value?.labels) return 40; const today = new Date().toISOString().substring(0,10); const idx = burnData.value.labels.indexOf(today); return idx >= 0 ? 40 + idx * burnStep.value : 40 })

// 对话框
const showTaskDlg = ref(false); const editingTask = ref(false); const savingTask = ref(false)
const showMsDlg = ref(false); const editingMs = ref(false); const savingMs = ref(false)
const showShDlg = ref(false); const editingSh = ref(false); const savingSh = ref(false)
const showRiskDlg = ref(false); const editingRisk = ref(false); const savingRisk = ref(false)
const showReport = ref(false); const reportContent = ref('')

const taskF = reactive({ id:null,title:'',description:'',assigneeId:null,status:'todo',priority:'normal',estimatedHours:null,progress:0,dueDate:null,tags:'' })
const msF = reactive({ id:null,projectId,project_id:projectId,name:'',description:'',targetDate:null,status:'pending' })
const shF = reactive({ id:null,projectId,project_id:projectId,name:'',role:'',department:'',influence:'normal',contact:'',expectations:'' })
const riskF = reactive({ id:null,projectId,project_id:projectId,title:'',category:'技术风险',probability:'medium',impact:'medium',status:'open',ownerId:null,mitigation:'',identifiedDate:null })

function sTag(s) { return s==='done'?'success':s==='in_progress'?'primary':s==='blocked'?'danger':'info' }
function sLabel(s) { return s==='done'?'已完成':s==='in_progress'?'进行中':s==='blocked'?'阻塞':'待办' }
function pColor(p) { return p>=80?'#67c23a':p>=50?'#409eff':'#e6a23c' }
function loadPct(m) { return m.taskCount ? Math.round(m.doneCount/m.taskCount*100) : 0 }
function fmtDate(d) { if(!d) return null; const dt=new Date(d); return `${dt.getFullYear()}-${String(dt.getMonth()+1).padStart(2,'0')}-${String(dt.getDate()).padStart(2,'0')}` }
function aiTitle(h) { return h==='green'?'🟢 项目健康':h==='yellow'?'🟡 需关注':'🔴 有风险' }
function aiType(h) { return h==='green'?'success':h==='yellow'?'warning':'error' }
function probLabel(p) { return p==='high'?'高':p==='medium'?'中':'低' }
function impLabel(i) { return i==='high'?'高':i==='medium'?'中':'低' }
function riskStatus(s) { return s==='open'?'待处理':s==='mitigating'?'缓解中':'已关闭' }

async function loadAll() {
  loading.value = true
  try {
    const [p,s,t,m,l,bc,dp,bi,hd,ms,sh,rk] = await Promise.all([
      getProject(projectId), getProjectStats(projectId), getTasks(projectId),
      getMembers(), getMemberLoad(projectId),
      getBurnChart(projectId).catch(()=>({data:null})),
      getDelayPrediction(projectId).catch(()=>({data:[]})),
      getBlockedImpact(projectId).catch(()=>({data:[]})),
      getHourDeviations(projectId).catch(()=>({data:[]})),
      getMilestones(projectId).catch(()=>({data:[]})),
      getStakeholders(projectId).catch(()=>({data:[]})),
      getRisks(projectId).catch(()=>({data:[]}))
    ])
    project.value=p.data; stats.value=s.data; tasks.value=t.data||[]
    members.value=m.data||[]; memberLoad.value=l.data||[]
    burnData.value=bc.data; delayPreds.value=dp.data||[]; blockedImpacts.value=bi.data||[]
    hourDevs.value=hd.data||[]; milestones.value=ms.data||[]; stakeholders.value=sh.data||[]
    risks.value=rk.data||[]
    loadReports()
  } finally { loading.value = false }
}

async function loadReports() {
  try { const r=await getReports(projectId); reports.value=(r.data||[]).slice(0,10) } catch(e){}
}

// 任务 CRUD
function openAddTask() { editingTask.value=false; Object.assign(taskF,{id:null,title:'',description:'',assigneeId:null,status:'todo',priority:'normal',estimatedHours:null,progress:0,dueDate:null,tags:''}); showTaskDlg.value=true }
function editTask(row) { editingTask.value=true; Object.assign(taskF,{id:row.id,title:row.title,description:row.description,assigneeId:row.assigneeId,status:row.status,priority:row.priority,estimatedHours:row.estimatedHours,progress:row.progress,dueDate:row.dueDate,tags:row.tags}); showTaskDlg.value=true }
async function saveTask() {
  if(!taskF.title){ ElMessage.warning('请输入标题'); return }
  savingTask.value=true
  try {
    const p={projectId,title:taskF.title,description:taskF.description,assigneeId:taskF.assigneeId,status:taskF.status,priority:taskF.priority,estimatedHours:taskF.estimatedHours,progress:taskF.progress,dueDate:fmtDate(taskF.dueDate),tags:taskF.tags}
    if(editingTask.value) await updateTask(taskF.id,p); else await createTask(p)
    ElMessage.success(editingTask.value?'更新成功':'新增成功')
    showTaskDlg.value=false; loadAll()
  } catch(e){ ElMessage.error('保存失败') } finally { savingTask.value=false }
}
async function delTask(id) { try { await deleteTask(id); ElMessage.success('删除成功'); loadAll() } catch(e){ ElMessage.error('删除失败') } }

// AI
async function runAIHealthCheck() {
  aiChecking.value=true; aiResult.value=null
  try { const r=await runHealthCheck(projectId); aiResult.value=r.data; ElMessage.success(r.data.success?'AI诊断完成':'诊断失败'); loadAll() } catch(e){ ElMessage.error('AI调用失败') } finally { aiChecking.value=false }
}
async function generateAIReport(type) {
  try { const r=await generateReport(projectId,type); if(r.data.success){ ElMessage.success('报告生成成功'); loadReports(); reportContent.value=r.data.content; showReport.value=true } else ElMessage.warning(r.data.error) } catch(e){ ElMessage.error('AI调用失败') }
}
async function viewReport(r) { try { const res=await getReportDetail(r.id); reportContent.value=res.data?.content||''; showReport.value=true } catch(e){ ElMessage.error('加载失败') } }
async function delReport(id) { try { await deleteReport(id); ElMessage.success('删除成功'); loadReports() } catch(e){ ElMessage.error('删除失败') } }

// 里程碑
function openAddMs() { editingMs.value=false; Object.assign(msF,{id:null,projectId,project_id:projectId,name:'',description:'',targetDate:null,status:'pending'}); showMsDlg.value=true }
function editMs(row) { editingMs.value=true; Object.assign(msF,row); msF.projectId=row.projectId; msF.project_id=row.projectId; showMsDlg.value=true }
async function saveMs() {
  if(!msF.name){ ElMessage.warning('请输入名称'); return }
  savingMs.value=true
  try {
    const p={projectId:msF.projectId||msF.project_id||projectId,name:msF.name,description:msF.description,targetDate:fmtDate(msF.targetDate),status:msF.status}
    if(editingMs.value) await updateMilestone(msF.id,p); else await createMilestone(p)
    ElMessage.success(editingMs.value?'更新成功':'新增成功')
    showMsDlg.value=false; loadAll()
  } catch(e){ ElMessage.error('保存失败') } finally { savingMs.value=false }
}
async function delMs(id) { try { await deleteMilestone(id); ElMessage.success('删除成功'); loadAll() } catch(e){ ElMessage.error('删除失败') } }

// 干系人
function openAddSh() { editingSh.value=false; Object.assign(shF,{id:null,projectId,project_id:projectId,name:'',role:'',department:'',influence:'normal',contact:'',expectations:''}); showShDlg.value=true }
function editSh(row) { editingSh.value=true; Object.assign(shF,row); shF.projectId=row.projectId; shF.project_id=row.projectId; showShDlg.value=true }
async function saveSh() {
  if(!shF.name){ ElMessage.warning('请输入姓名'); return }
  savingSh.value=true
  try {
    const p={projectId:shF.projectId||shF.project_id||projectId,name:shF.name,role:shF.role,department:shF.department,influence:shF.influence,contact:shF.contact,expectations:shF.expectations}
    if(editingSh.value) await updateStakeholder(shF.id,p); else await createStakeholder(p)
    ElMessage.success(editingSh.value?'更新成功':'新增成功')
    showShDlg.value=false; loadAll()
  } catch(e){ ElMessage.error('保存失败') } finally { savingSh.value=false }
}
async function delSh(id) { try { await deleteStakeholder(id); ElMessage.success('删除成功'); loadAll() } catch(e){ ElMessage.error('删除失败') } }

// 风险
function openAddRisk() { editingRisk.value=false; Object.assign(riskF,{id:null,projectId,project_id:projectId,title:'',category:'技术风险',probability:'medium',impact:'medium',status:'open',ownerId:null,mitigation:'',identifiedDate:null}); showRiskDlg.value=true }
function editRisk(row) { editingRisk.value=true; Object.assign(riskF,row); riskF.projectId=row.projectId; riskF.project_id=row.projectId; showRiskDlg.value=true }
async function saveRisk() {
  if(!riskF.title){ ElMessage.warning('请输入风险描述'); return }
  savingRisk.value=true
  try {
    const p={projectId:riskF.projectId||project_id||projectId,title:riskF.title,category:riskF.category,probability:riskF.probability,impact:riskF.impact,status:riskF.status,ownerId:riskF.ownerId,mitigation:riskF.mitigation,identifiedDate:fmtDate(riskF.identifiedDate)}
    if(editingRisk.value) await updateRisk(riskF.id,p); else await createRisk(p)
    ElMessage.success(editingRisk.value?'更新成功':'登记成功')
    showRiskDlg.value=false; loadAll()
  } catch(e){ ElMessage.error('保存失败') } finally { savingRisk.value=false }
}
async function delRisk(id) { try { await deleteRisk(id); ElMessage.success('删除成功'); loadAll() } catch(e){ ElMessage.error('删除失败') } }

onMounted(loadAll)
</script>

<style scoped>
.project-detail { max-width:1300px; margin:0 auto }
.top-bar { display:flex; justify-content:space-between; align-items:center; margin-bottom:24px }
.top-actions { display:flex; gap:10px }
.stats-row { display:flex; gap:16px; margin-bottom:24px }
.stat-card { background:#fff; border-radius:10px; padding:16px 20px; flex:1; text-align:center }
.stat-card.warn { background:#fef0f0 }
.stat-num { font-size:24px; font-weight:700; color:#303133 }
.stat-num.red { color:#f56c6c }
.stat-num.orange { color:#e6a23c }
.stat-name { font-size:12px; color:#909399; margin-bottom:8px }
.reports-section { margin-bottom:20px }
.reports-section h4 { margin-bottom:12px }
.report-list { display:flex; flex-wrap:wrap; gap:10px }
.report-item { background:#fff; border-radius:6px; padding:8px 14px; display:flex; align-items:center; gap:10px; cursor:pointer; font-size:13px }
.report-item:hover { background:#f5f7fa }
.report-date { color:#c0c4cc; font-size:12px }
.chart-svg { overflow-x:auto; padding:0 0 10px }
.chart-legend { display:flex; gap:24px; margin-bottom:8px; font-size:13px }
.legend-line.ideal { color:#909399 }
.legend-line.actual { color:#409eff }
.risk-list { display:flex; flex-direction:column; gap:10px; margin-bottom:20px }
.risk-item { background:#fff; border-radius:8px; padding:14px 18px; border-left:4px solid #c0c4cc }
.risk-item.risk-high { border-left-color:#f56c6c; background:#fef0f0 }
.risk-item.risk-medium { border-left-color:#e6a23c; background:#fdf6ec }
.risk-item.risk-low { border-left-color:#67c23a }
.risk-header { display:flex; align-items:center; gap:10px; margin-bottom:6px }
.risk-title { font-weight:600 }
.risk-meta { font-size:12px; color:#909399 }
.risk-body { font-size:13px; color:#606266; margin-bottom:4px }
.risk-suggestion { font-size:13px; color:#909399; margin:4px 0 0 }
.affected-list { margin-top:8px; padding:8px 0 0 16px; border-top:1px solid #ebeef5 }
.affected-item { font-size:13px; padding:3px 0; color:#606266 }
.affected-dist { font-size:11px; color:#c0c4cc; margin-left:8px }
.ms-item { display:flex; align-items:center; gap:10px; flex-wrap:wrap }
.ms-name { font-weight:600 }
.ms-desc { font-size:12px; color:#909399 }
.ms-actions { margin-left:auto }
.member-load { display:flex; flex-direction:column; gap:12px }
.load-bar-wrap { display:flex; align-items:center; gap:12px }
.load-name { width:70px; font-size:13px }
.load-bar { flex:1; height:20px; background:#ebeef5; border-radius:10px; overflow:hidden }
.load-fill { height:100%; background:linear-gradient(90deg,#409eff,#67c23a); border-radius:10px; transition:width .3s }
.load-info { width:160px; font-size:12px; color:#909399 }
</style>
