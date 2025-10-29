"# AptechMall" 
```markdown
## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java 17 or higher**
2. **MySQL 8.0+** running on `localhost:3306`
   - Database name: `test_db`
   - Username: `root`
   - Password: (empty or configure in application.properties)

3. **Redis 6.0+** running on `localhost:6379` ⚠️ **REQUIRED**
   - Used for JWT token blacklisting (logout functionality)
   - Without Redis, logout and refresh token features will not work properly

### Installing Redis:

**Windows:**
```bash
# Using Chocolatey
choco install redis-64

# Or download from: https://github.com/microsoftarchive/redis/releases
```

**Linux/Mac:**
```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# Mac with Homebrew
brew install redis

# Start Redis
redis-server
```

**Verify Redis is running:**
```bash
redis-cli ping
# Should return: PONG
```
```
