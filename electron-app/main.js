const { app, BrowserWindow, Menu } = require('electron')
const path = require('path')

const isDev = !app.isPackaged
const mode = process.argv.includes('--mode=admin') ? 'admin' : 'portal'

function createWindow() {
  const win = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    title: '法保通 - 智能法律服务平台',
    icon: path.join(__dirname, 'icon.png'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
    },
  })

  if (isDev) {
    const devUrl = mode === 'admin' ? 'http://localhost:3001' : 'http://localhost:3000'
    win.loadURL(devUrl)
    win.webContents.openDevTools({ mode: 'detach' })
  } else {
    const distPath = path.join(__dirname, '..', 'frontend', mode === 'admin' ? 'admin' : 'pc-portal', 'dist')
    win.loadFile(path.join(distPath, 'index.html'))
  }

  return win
}

const menuTemplate = [
  {
    label: '法保通',
    submenu: [
      { label: '用户端', click: () => { mode = 'portal'; createWindow() } },
      { label: '管理后台', click: () => { mode = 'admin'; createWindow() } },
      { type: 'separator' },
      { role: 'quit' },
    ],
  },
  { label: '编辑', submenu: [{ role: 'undo' }, { role: 'redo' }, { type: 'separator' }, { role: 'cut' }, { role: 'copy' }, { role: 'paste' }] },
  { label: '视图', submenu: [{ role: 'reload' }, { role: 'toggleDevTools' }, { type: 'separator' }, { role: 'resetZoom' }, { role: 'zoomIn' }, { role: 'zoomOut' }, { type: 'separator' }, { role: 'togglefullscreen' }] },
  { label: '帮助', submenu: [{ label: '关于法保通', click: () => { const { dialog } = require('electron'); dialog.showMessageBox({ type: 'info', title: '关于法保通', message: '法保通 v1.0.0\n智能法律服务平台' }) } }] },
]

app.whenReady().then(() => {
  Menu.setApplicationMenu(Menu.buildFromTemplate(menuTemplate))
  createWindow()
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
