import requests
import random

BASE_URL = "http://localhost:8080/api"

def call(method, url, headers=None, json=None):
    print(f"  [{method}] {url}")
    resp = requests.request(method, url, headers=headers, json=json)
    print(f"  Status: {resp.status_code}")
    if resp.status_code >= 400:
        print(f"  Error: {resp.text[:200] if resp.text else 'EMPTY'}")
    return resp

# 1. Admin login
print("\n1. Admin login...")
admin_login = call("POST", f"{BASE_URL}/login", json={"email": "admin@example.com", "password": "123456"})
admin_token = admin_login.json()['jwt-token']
admin_headers = {"Authorization": f"Bearer {admin_token}", "Content-Type": "application/json"}
print(f"  Token: {admin_token[:30]}...")

# 2. Admin get users
print("\n2. Admin get users...")
users_resp = call("GET", f"{BASE_URL}/admin/users?pageSize=100", headers=admin_headers)
users = users_resp.json()['content']
regular_users = [u for u in users if not any(r['roleName'] == 'ADMIN' for r in u.get('roles', []))]
selected_user = regular_users[0]
user_id = selected_user['userId']
user_email = selected_user['email']
print(f"  Selected: {user_email} (ID: {user_id})")

# 3. User login
print("\n3. User login...")
user_login = call("POST", f"{BASE_URL}/login", json={"email": user_email, "password": "123456"})
user_token = user_login.json()['jwt-token']
user_headers = {"Authorization": f"Bearer {user_token}", "Content-Type": "application/json"}
print(f"  Token: {user_token[:30]}...")

# 4. User get products
print("\n4. User get products...")
products_resp = call("GET", f"{BASE_URL}/public/products?pageSize=100", headers=user_headers)
products_data = products_resp.json()
products = products_data.get('content', [])
print(f"  Products: {len(products)}")
if len(products) == 0:
    print("  ERROR: No products found! Check if seeder ran correctly.")
    exit(1)

# 5. User get own cart
print("\n5. User get own cart...")
user_resp = call("GET", f"{BASE_URL}/public/users/{user_id}", headers=user_headers)
user_data = user_resp.json()
cart_id = user_data['cart']['cartId']
print(f"  Cart ID: {cart_id}")

# 6. Add products to cart
print("\n6. Add products to cart...")
sample_size = min(random.randint(2, 4), len(products))
selected_products = random.sample(products, sample_size)
for product in selected_products:
    quantity = random.randint(1, 3)
    call("POST", f"{BASE_URL}/public/carts/{cart_id}/products/{product['productId']}/quantity/{quantity}", headers=user_headers)
    print(f"  + {quantity}x {product['productName']}")

# 7. View cart
print("\n7. View cart...")
cart_resp = call("GET", f"{BASE_URL}/public/users/{user_email}/carts/{cart_id}", headers=user_headers)
cart = cart_resp.json()
for item in cart['products']:
    print(f"  - {item['productName']} x{item.get('quantity', 0)}")
print(f"  Total: ${cart['totalPrice']:.2f}")

# 8. Create order
print("\n8. Create order...")
order_resp = call("POST", f"{BASE_URL}/public/users/{user_email}/carts/{cart_id}/order", headers=user_headers, json={"paymentMethod": "Credit Card", "cardNumber": "4532-1111-2222-3333", "cvc": "123"})
order = order_resp.json()
order_id = order['orderId']
print(f"  Order ID: {order_id}")
print(f"  Email: {order.get('email', 'N/A')}")
print(f"  Date: {order.get('orderDate', 'N/A')}")
print(f"  Status: {order.get('orderStatus', 'N/A')}")
print(f"  Total: ${order.get('totalAmount', 0):.2f}")
print(f"  Payment: {order.get('payment', {}).get('paymentMethod', 'N/A')}")
print(f"  Items:")
for item in order.get('orderItems', []):
    print(f"    - {item.get('product', {}).get('productName', 'N/A')} x{item.get('quantity', 0)} @ ${item.get('orderedProductPrice', 0):.2f}")

# 9. Get order
print("\n9. Get order...")
order_details_resp = call("GET", f"{BASE_URL}/public/users/{user_email}/orders/{order_id}", headers=user_headers)
order_details = order_details_resp.json()
print(f"  Order ID: {order_details['orderId']}")
print(f"  Total: ${order_details.get('totalAmount', 0):.2f}")
print(f"  Items: {len(order_details.get('orderItems', []))}")
for item in order_details.get('orderItems', []):
    print(f"    - {item.get('product', {}).get('productName', 'N/A')} x{item.get('quantity', 0)}")

print("\nDone!")
