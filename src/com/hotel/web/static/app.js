const state = {
  user: null
};

const roleLabels = {
  admin: "管理员",
  user: "普通用户"
};

const toast = document.getElementById("toast");
const loginCard = document.getElementById("loginCard");
const sessionBar = document.getElementById("sessionBar");
const tabBar = document.getElementById("tabBar");
const appContent = document.getElementById("appContent");
const sessionName = document.getElementById("sessionName");
const sessionRole = document.getElementById("sessionRole");

function notify(message, isError = false) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  toast.style.background = isError ? "rgba(127, 29, 29, 0.95)" : "rgba(31, 42, 42, 0.92)";
  clearTimeout(notify.timer);
  notify.timer = setTimeout(() => toast.classList.add("hidden"), 2600);
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const data = await response.json();
  if (!response.ok || !data.ok) {
    throw new Error(data.message || "请求失败");
  }
  return data;
}

function toParams(form) {
  return new URLSearchParams(new FormData(form));
}

function authParams() {
  return new URLSearchParams(state.user || {});
}

function renderSession() {
  const loggedIn = !!state.user;
  loginCard.classList.toggle("hidden", loggedIn);
  sessionBar.classList.toggle("hidden", !loggedIn);
  tabBar.classList.toggle("hidden", !loggedIn);
  appContent.classList.toggle("hidden", !loggedIn);

  if (!loggedIn) {
    return;
  }

  sessionName.textContent = `${state.user.realName} / ${state.user.username}`;
  sessionRole.textContent = roleLabels[state.user.role] || state.user.role;

  document.querySelectorAll(".admin-only").forEach((el) => {
    el.classList.toggle("hidden", state.user.role !== "admin");
  });

  document.querySelectorAll(".user-only").forEach((el) => {
    el.classList.toggle("hidden", state.user.role !== "user");
  });

  activateTab(state.user.role === "admin" ? "rooms" : "booking");
}

function activateTab(tabName) {
  document.querySelectorAll(".tab").forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.tab === tabName);
  });
  document.querySelectorAll(".tab-pane").forEach((pane) => {
    pane.classList.toggle("active", pane.dataset.pane === tabName);
  });
}

function renderTable(containerId, rows, columns) {
  const container = document.getElementById(containerId);
  if (!rows.length) {
    container.innerHTML = '<div class="empty">当前没有数据。</div>';
    return;
  }

  const head = columns.map((col) => `<th>${col.label}</th>`).join("");
  const body = rows.map((row) => {
    const tds = columns.map((col) => `<td>${row[col.key] ?? ""}</td>`).join("");
    return `<tr>${tds}</tr>`;
  }).join("");
  container.innerHTML = `<table><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

function renderStats(data) {
  const view = document.getElementById("statsView");
  const roomHtml = data.roomStats.map((item) => `<div>${item.label}: ${item.value}</div>`).join("");
  const orderHtml = data.orderStats.map((item) => `<div>${item.label}: ${item.value}</div>`).join("");
  view.innerHTML = `
    <article class="stat-card">
      <strong>房间状态</strong>
      ${roomHtml}
    </article>
    <article class="stat-card">
      <strong>订单状态</strong>
      ${orderHtml}
    </article>
    <article class="stat-card">
      <strong>总收入</strong>
      <div>${data.income}</div>
    </article>
  `;
}

async function loadRooms(filters = new URLSearchParams()) {
  const data = await request(`/api/rooms?${filters.toString()}`);
  renderTable("roomsTable", data.rooms, [
    { key: "roomId", label: "ID" },
    { key: "roomNo", label: "房间号" },
    { key: "roomType", label: "房型" },
    { key: "price", label: "价格" },
    { key: "status", label: "状态" },
    { key: "remark", label: "备注" }
  ]);
}

async function loadMyOrders() {
  const query = new URLSearchParams({ userId: state.user.userId });
  const data = await request(`/api/orders?${query.toString()}`);
  renderTable("myOrdersTable", data.orders, [
    { key: "orderId", label: "订单 ID" },
    { key: "roomNo", label: "房间号" },
    { key: "roomType", label: "房型" },
    { key: "startDate", label: "入住日期" },
    { key: "endDate", label: "离店日期" },
    { key: "totalPrice", label: "总价" },
    { key: "status", label: "状态" }
  ]);
}

async function loadAllOrders(status = "") {
  const query = new URLSearchParams({ scope: "all", role: state.user.role, status });
  const data = await request(`/api/orders?${query.toString()}`);
  renderTable("allOrdersTable", data.orders, [
    { key: "orderId", label: "订单 ID" },
    { key: "username", label: "用户名" },
    { key: "roomNo", label: "房间号" },
    { key: "roomType", label: "房型" },
    { key: "startDate", label: "入住" },
    { key: "endDate", label: "离店" },
    { key: "totalPrice", label: "总价" },
    { key: "status", label: "状态" }
  ]);
}

async function loadStats() {
  const data = await request(`/api/stats?role=${encodeURIComponent(state.user.role)}`);
  renderStats(data);
}

document.getElementById("loginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const data = await request("/api/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: toParams(event.target)
    });
    state.user = data.user;
    renderSession();
    await loadRooms();
    if (state.user.role === "admin") {
      await loadAllOrders();
      await loadStats();
    } else {
      await loadMyOrders();
    }
    notify("登录成功");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("logoutBtn").addEventListener("click", () => {
  state.user = null;
  renderSession();
  notify("已退出登录");
});

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => activateTab(tab.dataset.tab));
});

document.getElementById("roomSearchForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await loadRooms(toParams(event.target));
    notify("房间查询完成");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("loadAvailableBtn").addEventListener("click", async () => {
  try {
    await loadRooms(new URLSearchParams({ status: "空闲" }));
    activateTab("rooms");
    notify("已筛选空闲房间");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("bookForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    Object.entries(state.user).forEach(([key, value]) => params.append(key, value));
    params.append("action", "book");
    await request("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadRooms();
    await loadMyOrders();
    notify("预订成功");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("loadMyOrdersBtn").addEventListener("click", async () => {
  try {
    await loadMyOrders();
    notify("订单已刷新");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("cancelMineForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    Object.entries(state.user).forEach(([key, value]) => params.append(key, value));
    params.append("action", "cancelMine");
    await request("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadMyOrders();
    notify("订单已取消");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("roomCreateForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    params.append("action", "create");
    params.append("role", state.user.role);
    await request("/api/rooms", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadRooms();
    notify("房间已新增");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("roomUpdateForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    params.append("action", "update");
    params.append("role", state.user.role);
    await request("/api/rooms", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadRooms();
    notify("房间已修改");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("roomDeleteForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    params.append("action", "delete");
    params.append("role", state.user.role);
    await request("/api/rooms", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadRooms();
    notify("房间已删除");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("allOrdersForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await loadAllOrders(new FormData(event.target).get("status") || "");
    notify("订单列表已刷新");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("adminOrderActionForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const params = toParams(event.target);
    params.append("role", state.user.role);
    await request("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: params
    });
    await loadAllOrders();
    await loadStats();
    notify("订单操作已执行");
  } catch (error) {
    notify(error.message, true);
  }
});

document.getElementById("loadStatsBtn").addEventListener("click", async () => {
  try {
    await loadStats();
    notify("统计已刷新");
  } catch (error) {
    notify(error.message, true);
  }
});
