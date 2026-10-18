import LoginForm from "./components/LoginForm";
import { useEffect, useState } from "react";
import {
  getShipments,
  type Shipment, type UserRole,
} from "./api";

import CreateShipmentForm
  from "./components/CreateShipmentForm";

import ShipmentDetails
  from "./components/ShipmentDetails";

function App() {
  const [token, setToken] = useState<string | null>(null);
  const [role, setRole] = useState<UserRole | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [shipments, setShipments] =
      useState<Shipment[]>([]);

  const [selectedShipment, setSelectedShipment] =
      useState<Shipment | null>(null);

  const [loading, setLoading] =
      useState(true);

  const [error, setError] =
      useState<string | null>(null);

  async function loadShipments(page = 0) {
    if (!token) {
      return;
    }
    try {
      setLoading(true);
      setError(null);

      const data = await getShipments(token, page);

      setShipments(data.content);
      setCurrentPage(data.number);
      setTotalPages(data.totalPages);
    } catch {
      setError("Could not load shipments");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!token) {
      return;
    }
    loadShipments(0);
  }, [token]);

  function handleLogin(token: string, role: UserRole) {
    setToken(token);
    setRole(role);
  }

  function handleLogout(){
    setToken(null);
    setRole(null);
    setSelectedShipment(null);
    setShipments([]);
  }

  function handleShipmentCreated() {
    loadShipments();
  }

  function handleShipmentUpdated(
      updatedShipment: Shipment
  ) {
    setSelectedShipment(updatedShipment);

    setShipments((currentShipments) =>
        currentShipments.map((shipment) =>
            shipment.id === updatedShipment.id
                ? updatedShipment
                : shipment
        )
    );
  }
  if (!token) {
    return <LoginForm onLogin={handleLogin} />;
  }

  if (loading) {
    return <p>Loading shipments...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  if (selectedShipment) {
    return (
        <ShipmentDetails
            shipment={selectedShipment}
            onUpdated={handleShipmentUpdated}
            onBack={() =>
                setSelectedShipment(null)
            }
            token={token}
            isAdmin={role === "ADMIN"}
        />
    );
  }


  return (
      <div>

        <h1>ParcelFlow</h1>

        <button onClick={handleLogout}>
          Logout
        </button>

        <CreateShipmentForm
            onCreated={handleShipmentCreated}
            token={token}
        />

        <hr />

        <h2>Shipments</h2>

        {shipments.length === 0 && (
            <p>
              No shipments found.
            </p>
        )}

        {shipments.map((shipment) => (

            <div
                key={shipment.id}
                onClick={() =>
                    setSelectedShipment(shipment)
                }
                style={{
                  cursor: "pointer",
                  border: "1px solid #ccc",
                  padding: "16px",
                  marginBottom: "10px",
                }}
            >

              <h3>
                {shipment.trackingNumber}
              </h3>

              <p>
                {shipment.origin}
                {" → "}
                {shipment.destination}
              </p>

              <p>
                Status: {shipment.status}
              </p>

            </div>

        ))}

        {totalPages > 1 && (
            <div>
              <button
                  onClick={() => loadShipments(currentPage - 1)}
                  disabled={currentPage === 0}
              >
                Previous
              </button>

              <span>
            {" "}
                Page {currentPage + 1} of {totalPages}{" "}
        </span>

              <button
                  onClick={() => loadShipments(currentPage + 1)}
                  disabled={currentPage >= totalPages - 1}
              >
                Next
              </button>
            </div>
        )}

      </div>
  );
}

export default App;