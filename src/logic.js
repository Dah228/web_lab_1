const canvas = document.getElementById('picture');
const ctx = canvas.getContext('2d');


// Рисуем пример, когда R = 150
// треугольник
ctx.moveTo(50, 200);
ctx.lineTo(200, 350);
ctx.lineTo(200, 200);
ctx.closePath();
ctx.fillStyle = 'red'
ctx.fill();

// прямоугольник
ctx.lineTo(200, 275);
ctx.lineTo(350, 275);
ctx.lineTo(350, 200);
ctx.closePath();
ctx.fill();

// четверть круга
ctx.beginPath();
ctx.moveTo(200, 200);
ctx.lineTo(200, 125);
ctx.arc(200, 200, 75, -Math.PI / 2, 0);
ctx.closePath();
ctx.fill();

ctx.strokeStyle = "black";
ctx.beginPath();
//ось х
ctx.moveTo(0, 200);
ctx.lineTo(400, 200);
// Ось У
ctx.moveTo(200, 0);
ctx.lineTo(200, 400);
// Стрелочки к осям
ctx.moveTo(195, 5)
ctx.lineTo(200, 0);
ctx.moveTo(205, 5)
ctx.lineTo(200, 0);
ctx.moveTo(395, 195);
ctx.lineTo(400, 200);
ctx.moveTo(395, 205);
ctx.lineTo(400, 200);
ctx.stroke();

// Настройки для текста
ctx.fillStyle = 'black';
ctx.font = '14px Arial';
ctx.textAlign = 'center';
ctx.textBaseline = 'middle';

// Подписи на оси Y
ctx.fillText('R', 207, 42);        // R сверху
ctx.fillText('R/2', 213, 118);     // R/2
ctx.fillText('-R/2', 215, 283);    // -R/2
ctx.fillText('-R', 210, 360);      // -R снизу

// Подписи на оси X
ctx.fillText('-R', 40, 190);       // -R слева
ctx.fillText('-R/2', 120, 190);    // -R/2
ctx.fillText('R/2', 285, 190);     // R/2
ctx.fillText('R', 360, 190);       // R справа

// Подписи осей (x и y)
ctx.fillText('x', 390, 190);       // буква x на конце оси X
ctx.fillText('y', 210, 10);        // буква y на конце оси Y

// Настройки для штрихов
ctx.strokeStyle = 'black';
ctx.lineWidth = 1;

// Штрихи на оси X (вертикальные)
ctx.beginPath();
// -R
ctx.moveTo(50, 195);
ctx.lineTo(50, 205);
// -R/2
ctx.moveTo(125, 195);
ctx.lineTo(125, 205);
// R/2
ctx.moveTo(275, 195);
ctx.lineTo(275, 205);
// R
ctx.moveTo(350, 195);
ctx.lineTo(350, 205);
ctx.stroke();

// Штрихи на оси Y (горизонтальные)
ctx.beginPath();
// R
ctx.moveTo(195, 50);
ctx.lineTo(205, 50);
// R/2
ctx.moveTo(195, 125);
ctx.lineTo(205, 125);
// -R/2
ctx.moveTo(195, 275);
ctx.lineTo(205, 275);
// -R
ctx.moveTo(195, 350);
ctx.lineTo(205, 350);
ctx.stroke();


const tbody = document.getElementById('results-body');
const coord_X = document.getElementById('coord_X');
const coord_Y = document.getElementById('coord_Y');
const radius_R = document.getElementById('radius_R');
const button_send = document.getElementById('send_button');


function check_not_null(coord_Y, radius_R) {
    return coord_Y !== "" && radius_R !== "";
}

function check_number(coord_Y, radius_R) {
    return !isNaN(coord_Y) && !isNaN(radius_R);
}

function check_range(coord_Y, radius_R) {
    return (parseFloat(coord_Y) >= -5 && parseFloat(coord_Y) <= 3) && (parseFloat(radius_R) >= 1 && parseFloat(radius_R) <= 4);
}

function input_is_correct(coord_Y, radius_R) {
    return check_not_null(coord_Y, radius_R) && check_number(coord_Y, radius_R) && check_range(coord_Y, radius_R);
}

function is_inside_circle(coord_X, coord_Y, radius_R) {
    return parseFloat(coord_Y) > 0 && coord_X >= 0 && coord_X ** 2 + parseFloat(coord_Y) ** 2 <= (radius_R ** 2) / 4;
}

function is_inside_triangle(coord_X, coord_Y, radius_R) {
    return parseFloat(coord_Y) <= 0 && coord_X <= 0 && (-parseFloat(coord_Y) - coord_X <= radius_R);
}

function is_inside_rectangle(coord_X, coord_Y, radius_R) {
    return parseFloat(coord_Y) <= 0 && coord_X > 0 && Math.abs(parseFloat(coord_Y)) <= radius_R / 2 && coord_X <= radius_R;
}


function is_inside_zone(coord_X, coord_Y, radius_R) {
    if (input_is_correct(coord_Y, radius_R)) {
        return is_inside_circle(coord_X, coord_Y, radius_R) || is_inside_triangle(coord_X, coord_Y, radius_R) || is_inside_rectangle(coord_X, coord_Y, radius_R);
    }
    return false;

}


function try_f() {
    const x = coord_X.value;
    const y = coord_Y.value;
    const r = radius_R.value;

    if (!input_is_correct(y, r)) {
        alert("Ошибка: введите корректные числа в диапазоне!");
        return;
    }

    const res = is_inside_zone(x, y, r);
    const timestamp = Date.now();
    add_element_to_table(x, y, r, res, timestamp);
    saveToLocalStorage(x, y, r, res, timestamp);
}


button_send.addEventListener('click', try_f);


function add_element_to_table(x, y, r, isHit, timestamp) {
    const tr = document.createElement('tr');
    const russian_date = new Date(timestamp).toLocaleString('ru-RU');
    const resultText = isHit ? 'Попала' : 'Не попала';
    const color = isHit ? 'green' : 'red';
    tr.innerHTML = `
        <td>${x}</td>
        <td>${y}</td>
        <td>${r}</td>
        <td style="color: ${color}; font-weight: bold;">${resultText}</td>
        <td>${russian_date}</td>
    `;
    tbody.appendChild(tr);
    return timestamp;
}


function saveToLocalStorage(x, y, r, isHit, timestamp) {
    const saved_data = localStorage.getItem('savedPoints');
    let old_uploaded_data = saved_data ? JSON.parse(saved_data) : [];
    const new_card = {
        x: x,
        y: y,
        r: r,
        isHit: isHit,
        timestamp: timestamp
    };

    old_uploaded_data.push(new_card);
    const new_data_for_load = JSON.stringify(old_uploaded_data);
    localStorage.setItem('savedPoints', new_data_for_load);
}


function loadFromLocalStorage(){
    const saved_data = localStorage.getItem("savedPoints");
    let old_uploaded_data = saved_data ? JSON.parse(saved_data) : [];
    old_uploaded_data.forEach((item) => {
        add_element_to_table(item.x,item.y,item.r,item.isHit,item.timestamp);
    });
}


loadFromLocalStorage();
