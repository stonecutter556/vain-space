<template>
  <div class="cloth-sim">
    <div ref="canvasContainer" class="canvas-container"></div>
    <div class="sim-overlay">
      <div class="sim-controls">
        <button class="back-btn" @click="goBack">← 返回</button>
        <div class="control-group">
          <span class="label">风力</span>
          <input type="range" min="0" max="30" step="0.5" :value="windSpeed" @input="windSpeed = +$event.target.value" />
          <span class="value">{{ windSpeed.toFixed(1) }}</span>
        </div>
        <div class="control-group">
          <span class="label">重力</span>
          <input type="range" min="0" max="50" step="0.5" :value="gravity" @input="gravity = +$event.target.value" />
          <span class="value">{{ gravity.toFixed(1) }}</span>
        </div>
        <div class="control-group">
          <span class="label">布料柔软度</span>
          <input type="range" min="0.1" max="2" step="0.1" :value="softness" @input="softness = +$event.target.value" />
          <span class="value">{{ softness.toFixed(1) }}</span>
        </div>
        <div class="hint">🖱 拖拽布料交互</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'

const router = useRouter()
const canvasContainer = ref(null)

const windSpeed = ref(9)
const gravity = ref(20)
const softness = ref(1)

let scene, camera, renderer, clothMesh, shadowMesh
let particles = []
let constraints = []
let mouse = new THREE.Vector2()
let mouse3D = new THREE.Vector3()
let raycaster = new THREE.Raycaster()
let isDragging = false
let draggedParticle = null
let animationId = null
let clock = new THREE.Clock()

const COLS = 25
const ROWS = 50
const SEG_W = 0.22
const SEG_H = 0.22
const DRAG_COEFF = 0.985
const SUBSTEPS = 8

class Particle {
  constructor(x, y, z, row, col) {
    this.pos = new THREE.Vector3(x, y, z)
    this.prev = new THREE.Vector3(x, y, z)
    this.acc = new THREE.Vector3()
    this.pinned = false
    this.row = row
    this.col = col
  }
  integrate(dt) {
    if (this.pinned) return
    const vel = this.pos.clone().sub(this.prev).multiplyScalar(DRAG_COEFF)
    this.prev.copy(this.pos)
    this.pos.add(vel).add(this.acc.clone().multiplyScalar(dt * dt))
    this.acc.set(0, 0, 0)
  }
  applyForce(f) {
    this.acc.add(f.clone().divideScalar(1))
  }
}

function initScene() {
  const container = canvasContainer.value
  const w = window.innerWidth
  const h = window.innerHeight

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0a0a0f)

  camera = new THREE.PerspectiveCamera(45, w / h, 0.1, 100)
  camera.position.set(0, 2, 14)
  camera.lookAt(0, -1, 0)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  const ambLight = new THREE.AmbientLight(0x404060, 0.6)
  scene.add(ambLight)

  const dirLight = new THREE.DirectionalLight(0x6a9ef0, 1.2)
  dirLight.position.set(5, 10, 7)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.set(2048, 2048)
  scene.add(dirLight)

  const fillLight = new THREE.DirectionalLight(0xa27cf0, 0.4)
  fillLight.position.set(-5, 3, 5)
  scene.add(fillLight)

  const hemiLight = new THREE.HemisphereLight(0x6a9ef0, 0x0a0a0f, 0.4)
  scene.add(hemiLight)

  createCloth()
  createShadow()

  window.addEventListener('resize', onResize)
  window.addEventListener('mousedown', onMouseDown)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
  window.addEventListener('touchstart', onTouchStart, { passive: false })
  window.addEventListener('touchmove', onTouchMove, { passive: false })
  window.addEventListener('touchend', onTouchEnd)

  animate()
}

function createCloth() {
  particles = []
  constraints = []

  const cols = COLS, rows = ROWS
  const sw = SEG_W, sh = SEG_H
  const totalW = cols * sw, totalH = rows * sh

  for (let r = 0; r <= rows; r++) {
    for (let c = 0; c <= cols; c++) {
      const x = c * sw - totalW / 2
      const y = -r * sh + totalH / 2
      const z = 0
      const p = new Particle(x, y, z, r, c)
      if (r === 0) p.pinned = true
      particles.push(p)
    }
  }

  for (let r = 0; r <= rows; r++) {
    for (let c = 0; c <= cols; c++) {
      const idx = r * (cols + 1) + c
      if (c < cols) constraints.push({ a: idx, b: idx + 1, rest: sw })
      if (r < rows) constraints.push({ a: idx, b: idx + (cols + 1), rest: sh })
    }
  }

  const geo = new THREE.BufferGeometry()
  const vertices = new Float32Array((cols + 1) * (rows + 1) * 3)
  const uvs = new Float32Array((cols + 1) * (rows + 1) * 2)
  const indices = []

  for (let r = 0; r <= rows; r++) {
    for (let c = 0; c <= cols; c++) {
      const i = r * (cols + 1) + c
      uvs[i * 2] = c / cols
      uvs[i * 2 + 1] = 1 - r / rows
    }
  }

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const a = r * (cols + 1) + c
      const b = r * (cols + 1) + c + 1
      const d = (r + 1) * (cols + 1) + c
      const e = (r + 1) * (cols + 1) + c + 1
      indices.push(a, b, d, b, e, d)
    }
  }

  geo.setAttribute('position', new THREE.BufferAttribute(vertices, 3))
  geo.setAttribute('uv', new THREE.BufferAttribute(uvs, 2))
  geo.setIndex(indices)
  geo.computeVertexNormals()

  const texCanvas = document.createElement('canvas')
  texCanvas.width = 512
  texCanvas.height = 512
  const ctx = texCanvas.getContext('2d')

  const gradient = ctx.createLinearGradient(0, 0, 512, 512)
  gradient.addColorStop(0, '#1a1a2e')
  gradient.addColorStop(0.3, '#16213e')
  gradient.addColorStop(0.6, '#0f3460')
  gradient.addColorStop(1, '#1a1a2e')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, 512, 512)

  ctx.fillStyle = 'rgba(106, 158, 240, 0.08)'
  for (let i = 0; i < 50; i++) {
    const x = Math.random() * 512
    const y = Math.random() * 512
    const r = Math.random() * 60 + 10
    ctx.beginPath()
    ctx.arc(x, y, r, 0, Math.PI * 2)
    ctx.fill()
  }

  ctx.strokeStyle = 'rgba(106, 158, 240, 0.15)'
  ctx.lineWidth = 1
  for (let i = 0; i < 8; i++) {
    const x = Math.random() * 512
    const y = Math.random() * 512
    ctx.beginPath()
    ctx.moveTo(x, y)
    ctx.lineTo(x + (Math.random() - 0.5) * 100, y + (Math.random() - 0.5) * 100)
    ctx.stroke()
  }

  const texture = new THREE.CanvasTexture(texCanvas)
  texture.wrapS = texture.wrapT = THREE.RepeatWrapping

  const mat = new THREE.MeshStandardMaterial({
    map: texture,
    side: THREE.DoubleSide,
    roughness: 0.4,
    metalness: 0.1,
    emissive: new THREE.Color(0x1a2a4a),
    emissiveIntensity: 0.1
  })

  clothMesh = new THREE.Mesh(geo, mat)
  clothMesh.castShadow = true
  clothMesh.receiveShadow = true
  scene.add(clothMesh)
}

function createShadow() {
  const geo = new THREE.PlaneGeometry(10, 14)
  const mat = new THREE.ShadowMaterial({
    opacity: 0.25,
    color: 0x6a9ef0
  })
  shadowMesh = new THREE.Mesh(geo, mat)
  shadowMesh.position.set(0, -5, -2)
  shadowMesh.rotation.x = -Math.PI / 2
  shadowMesh.receiveShadow = false
  scene.add(shadowMesh)
}

function simulate() {
  const cols = COLS, rows = ROWS
  const dt = 0.016
  const subDt = dt / SUBSTEPS

  const windAngle = (windSpeed.value * 0.05) * Math.PI / 180
  const windDir = new THREE.Vector3(
    Math.cos(windAngle + 0.2) * windSpeed.value * 0.02,
    -Math.random() * 0.005,
    Math.sin(windAngle) * windSpeed.value * 0.01
  )
  const gravityVec = new THREE.Vector3(0, -gravity.value * 0.015, 0)

  for (let s = 0; s < SUBSTEPS; s++) {
    for (const p of particles) {
      if (p.pinned) continue
      p.applyForce(windDir)
      p.applyForce(gravityVec)
      p.integrate(subDt)
    }

    for (let i = 0; i < 3; i++) {
      for (const c of constraints) {
        const a = particles[c.a]
        const b = particles[c.b]
        if (a.pinned && b.pinned) continue
        const delta = b.pos.clone().sub(a.pos)
        const dist = delta.length()
        if (dist < 1e-8) continue
        const diff = (dist - c.rest) / dist * 0.8
        const move = delta.multiplyScalar(diff * 0.5)
        if (!a.pinned) a.pos.add(move)
        if (!b.pinned) b.pos.sub(move)
      }
    }

    if (isDragging && draggedParticle) {
      draggedParticle.pos.lerp(mouse3D, 0.3)
      draggedParticle.prev.copy(draggedParticle.pos)
    }
  }
}

function updateGeometry() {
  const cols = COLS, rows = ROWS
  const pos = clothMesh.geometry.attributes.position
  const array = pos.array

  for (let r = 0; r <= rows; r++) {
    for (let c = 0; c <= cols; c++) {
      const i = r * (cols + 1) + c
      const p = particles[i]
      array[i * 3] = p.pos.x
      array[i * 3 + 1] = p.pos.y
      array[i * 3 + 2] = p.pos.z
    }
  }

  pos.needsUpdate = true
  clothMesh.geometry.computeVertexNormals()
}

function findClosestParticle(x, y) {
  const cols = COLS
  let minDist = Infinity
  let closest = null

  const mouseVec = new THREE.Vector3(x, y, 0)

  for (const p of particles) {
    if (p.pinned) continue
    const dist = mouseVec.distanceTo(p.pos)
    if (dist < minDist) {
      minDist = dist
      closest = p
    }
  }

  return minDist < 3 ? closest : null
}

function onMouseDown(e) {
  const rect = renderer.domElement.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const y = -((e.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(new THREE.Vector2(x, y), camera)
  const intersects = raycaster.intersectObject(clothMesh)
  if (intersects.length > 0) {
    const point = intersects[0].point
    const p = findClosestParticle(point.x, point.y)
    if (p) {
      isDragging = true
      draggedParticle = p
      mouse3D.copy(point)
    }
  }
}

function onMouseMove(e) {
  const rect = renderer.domElement.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const y = -((e.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(new THREE.Vector2(x, y), camera)
  const intersects = raycaster.intersectObject(clothMesh)
  if (intersects.length > 0) {
    mouse3D.copy(intersects[0].point)
  }

  if (isDragging && draggedParticle) {
    mouse3D.copy(intersects.length > 0 ? intersects[0].point : mouse3D)
  }
}

function onMouseUp() {
  isDragging = false
  draggedParticle = null
}

function onTouchStart(e) {
  e.preventDefault()
  const touch = e.touches[0]
  onMouseDown({ clientX: touch.clientX, clientY: touch.clientY })
}

function onTouchMove(e) {
  e.preventDefault()
  const touch = e.touches[0]
  onMouseMove({ clientX: touch.clientX, clientY: touch.clientY })
}

function onTouchEnd() {
  onMouseUp()
}

function animate() {
  animationId = requestAnimationFrame(animate)
  simulate()
  updateGeometry()
  renderer.render(scene, camera)
}

function onResize() {
  const w = window.innerWidth
  const h = window.innerHeight
  camera.aspect = w / h
  camera.updateProjectionMatrix()
  renderer.setSize(w, h)
}

function goBack() {
  router.push({ name: 'Home' })
}

onMounted(() => {
  initScene()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    renderer.domElement.remove()
  }
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousedown', onMouseDown)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
})
</script>

<style scoped>
.cloth-sim {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  background: #0a0a0f;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.sim-overlay {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1001;
}

.sim-controls {
  background: rgba(20, 20, 30, 0.8);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 20px;
  width: 240px;
}

.back-btn {
  background: rgba(106, 158, 240, 0.15);
  border: 1px solid rgba(106, 158, 240, 0.2);
  color: #6a9ef0;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  width: 100%;
  margin-bottom: 16px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(106, 158, 240, 0.25);
}

.control-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.control-group .label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  width: 55px;
  flex-shrink: 0;
}

.control-group input[type="range"] {
  flex: 1;
  height: 4px;
  accent-color: #6a9ef0;
  cursor: pointer;
}

.control-group .value {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  width: 32px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  text-align: center;
  margin-top: 8px;
}
</style>
