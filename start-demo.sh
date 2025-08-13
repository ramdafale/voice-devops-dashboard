#!/bin/bash

# Voice DevOps Dashboard - Quick Start Script
# This script starts all services for the voice-controlled DevOps dashboard demo

echo "🚀 Starting Voice DevOps Dashboard Demo..."
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install docker-compose and try again."
    exit 1
fi

# Stop any existing containers
echo "🛑 Stopping any existing containers..."
docker-compose down

# Build and start all services
echo "🔨 Building and starting all services..."
docker-compose up -d --build

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service status
echo "📊 Checking service status..."
docker-compose ps

# Display access URLs
echo ""
echo "🎉 Voice DevOps Dashboard Demo is ready!"
echo "=========================================="
echo ""
echo "📱 Access URLs:"
echo "   • Main Dashboard:     http://localhost:8080"
echo "   • Admin Dashboard:    http://localhost:8080/admin-dashboard.html"
echo "   • User Dashboard:     http://localhost:8080/user-dashboard.html"
echo "   • Mock Jenkins:       http://localhost:8081"
echo "   • Mock GitHub:        http://localhost:8082"
echo ""
echo "🎤 Voice Commands Demo:"
echo "   • Click 'Start Voice' button in any portal"
echo "   • Try commands like:"
echo "     - 'Build my feature branch'"
echo "     - 'Deploy feature-branch to staging'"
echo "     - 'Create pull request for feature-branch'"
echo "     - 'Approve build 1234'"
echo ""
echo "📚 Documentation:"
echo "   • README.md - Main documentation"
echo "   • INTEGRATION_GUIDE.md - Complete use cases"
echo ""
echo "🛠️  Useful Commands:"
echo "   • View logs:          docker-compose logs -f"
echo "   • Stop services:      docker-compose down"
echo "   • Restart services:   docker-compose restart"
echo ""
echo "🎯 Demo Scenarios:"
echo "   1. Developer workflow (create PR → build → deploy)"
echo "   2. Admin production deployment (deploy → approve → monitor)"
echo "   3. Code review and merge (review → merge → build)"
echo "   4. Emergency rollback (abort → rollback → verify)"
echo "   5. Team performance monitoring (stats → reports)"
echo ""
echo "✨ Enjoy the Voice DevOps Dashboard Demo!" 