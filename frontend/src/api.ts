export interface Shipment {
    id: number;
    trackingNumber: string;
    senderName: string;
    recipientName: string;
    origin: string;
    destination: string;
    status: ShipmentStatus;
    createdAt: string;
    estimatedDelivery: string | null;
}

export interface CreateShipmentRequest {
    senderName: string;
    recipientName: string;
    origin: string;
    destination: string;
    estimatedDelivery?: string | null;
}

export type ShipmentStatus =
    | "CREATED"
    | "PICKED_UP"
    | "IN_TRANSIT"
    | "OUT_FOR_DELIVERY"
    | "DELIVERED"
    | "EXCEPTION"
    | "CANCELLED";

const API_URL = "http://localhost:8080/api";

export interface ShipmentPage {
    content: Shipment[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export type UserRole = "USER" | "ADMIN";

export interface LoginResponse {
    token: string;
    role: UserRole;
}
export async function getShipments(
    token: string,
    page = 0,
    size = 3,
    status?: ShipmentStatus
): Promise<ShipmentPage> {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (status) {
        params.append("status", status);
    }

    const response = await fetch(
        `${API_URL}/shipments?${params.toString()}`,
        {
            headers:{
                Authorization: `Bearer ${token}`,
            }
        }
    );

    if (!response.ok) {
        throw new Error("Failed to fetch shipments");
    }

    return response.json();
}

export async function getShipment(id: number): Promise<Shipment> {
    const response = await fetch(`${API_URL}/shipments/${id}`);

    if (!response.ok) {
        throw new Error("Failed to fetch shipment");
    }

    return response.json();
}

export async function updateShipmentStatus(
    token: string,
    id: number,
    status: ShipmentStatus
): Promise<Shipment> {

    const response = await fetch(`${API_URL}/shipments/${id}/status`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status }),
    });

    if (!response.ok) {
        throw new Error("Failed to update shipment status");
    }

    return response.json();
}

export async function login(
    username: string,
    password: string
): Promise<LoginResponse> {

    const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            username,
            password,
        }),
    });

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error("Invalid username or password");
        }

        throw new Error("Login failed");
    }

    return response.json();
}

export async function createShipment(
    token: string,
    request: CreateShipmentRequest
): Promise<Shipment> {

    const response = await fetch(`${API_URL}/shipments`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(request),
    });

    if (!response.ok) {
        throw new Error("Failed to create shipment");
    }

    return response.json();
}