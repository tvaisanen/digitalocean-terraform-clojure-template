resource "digitalocean_project" "project" {
  name        = "my-sample-project"
  description = "description"
  environment = "development"
  purpose     = "template-app"
}

resource "digitalocean_container_registry" "app_registry" {
  name                   = "clojure-sample-app"
  subscription_tier_slug = "starter"
}

resource "digitalocean_app" "app" {
  spec {
    name   = "sample-app"
    region = "ams"

    alert {
      rule = "DEPLOYMENT_FAILED"
    }

    static_site {
      name            = "web"
      source_dir      = "web"
      output_dir      = "/web/public"
      dockerfile_path = "web/Dockerfile"

      git {
        branch         = "blog-post-series-04"
        repo_clone_url = "https://github.com/tvaisanen/digitalocean-terraform-clojure-template.git"
      }
    }

    service {
      name               = "api"
      instance_count     = 1
      instance_size_slug = "basic-xxs"

      image {
        registry_type = "DOCR"
        repository    = "dev"
        tag           = "latest"
        deploy_on_push {
          enabled = true
        }
      }

      env {
        key   = "JDBC_DATABASE_URL"
        value = "$${starter-db.JDBC_DATABASE_URL}"
      }

      source_dir = "api/"
      http_port  = 8000

      run_command = "clojure -X:run"
    }

    database {
      name       = "starter-db"
      engine     = "PG"
      production = false
    }

    ingress {

      rule {
        component {
          name = "api"
        }
        match {
          path {
            prefix = "/api"
          }
        }
      }
      rule {
        component {
          name = "web"
        }
        match {
          path {
            prefix = "/"
          }
        }
      }
    }
  }
}

output "app_url" {
  value = digitalocean_app.app.live_url
}
